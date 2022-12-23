package org.elasticsearch.repositories.gcs;

import fixture.gcs.FakeOAuth2HttpHandler;
import fixture.gcs.GoogleCloudStorageHttpHandler;
import fixture.gcs.TestUtils;

import com.sun.net.httpserver.HttpServer;

import org.elasticsearch.common.settings.MockSecureSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.mocksocket.MockHttpServer;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.repositories.blobstore.ESBlobStoreRepositoryIntegTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static org.elasticsearch.repositories.gcs.GoogleCloudStorageClientSettings.CREDENTIALS_FILE_SETTING;
import static org.elasticsearch.repositories.gcs.GoogleCloudStorageClientSettings.ENDPOINT_SETTING;
import static org.elasticsearch.repositories.gcs.GoogleCloudStorageClientSettings.PROXY_HOST_SETTING;
import static org.elasticsearch.repositories.gcs.GoogleCloudStorageClientSettings.PROXY_PORT_SETTING;
import static org.elasticsearch.repositories.gcs.GoogleCloudStorageClientSettings.PROXY_TYPE_SETTING;
import static org.elasticsearch.repositories.gcs.GoogleCloudStorageClientSettings.TOKEN_URI_SETTING;
import static org.elasticsearch.repositories.gcs.GoogleCloudStorageRepository.BUCKET;
import static org.elasticsearch.repositories.gcs.GoogleCloudStorageRepository.CLIENT_NAME;

public class GcsProxyIntegrationTests extends ESBlobStoreRepositoryIntegTestCase {

    private static HttpServer httpServer;
    private static MockHttpProxyServer proxyServer;

    @BeforeClass
    public static void startHttpServer() throws Exception {
        httpServer = MockHttpServer.createHttp(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        httpServer.start();
        proxyServer = new MockHttpProxyServer(new MockHttpProxyServer.SocketRequestHandler() {

            @Override
            public void handle(InputStream is, OutputStream os) throws IOException {
                try (var bis = new BufferedInputStream(is); var bos = new BufferedOutputStream(os)) {
                    String requestLine = readLine(bis);
                    String[] requestLineParts = requestLine.split(" ");
                    String requestMethod = requestLineParts[0];
                    String url = requestLineParts[1];
                    var originHttpConnection = (HttpURLConnection) new URL(url).openConnection();
                    originHttpConnection.setRequestMethod(requestMethod);
                    int requestContentLength = -1;
                    boolean requestChunked = false;
                    while (true) {
                        String header = readLine(bis);
                        if (header.isEmpty()) {
                            break;
                        }
                        String[] parts = header.split(":");
                        String name = parts[0].trim();
                        String value = parts[1].trim();
                        originHttpConnection.setRequestProperty(name, value);
                        if (name.equalsIgnoreCase("Content-Length")) {
                            requestContentLength = Integer.parseInt(value);
                        } else if (name.equalsIgnoreCase("Transfer-Encoding") && value.equalsIgnoreCase("chunked")) {
                            requestChunked = true;
                        }
                    }
                    if (requestContentLength > 0) {
                        originHttpConnection.setDoOutput(true);
                        byte[] bb = new byte[requestContentLength];
                        int len = bis.readNBytes(bb, 0, requestContentLength);
                        try (var hos = originHttpConnection.getOutputStream()) {
                            hos.write(bb, 0, len);
                        }
                    } else if (requestChunked) {
                        originHttpConnection.setDoOutput(true);
                        try (var hos = originHttpConnection.getOutputStream()) {
                            while (true) {
                                String line = readLine(bis);
                                if (line.isEmpty()) {
                                    break;
                                }
                                int chunkSize = Integer.parseInt(line, 16);
                                if (chunkSize == 0) {
                                    if (bis.read() != '\r' || bis.read() != '\n') {
                                        throw new IllegalStateException("Not CRLF");
                                    }
                                    break;
                                }
                                byte[] bb = new byte[chunkSize];
                                int len = bis.readNBytes(bb, 0, chunkSize);
                                if (len == -1) {
                                    break;
                                }
                                hos.write(bb, 0, len);
                                if (bis.read() != '\r' || bis.read() != '\n') {
                                    throw new IllegalStateException("Not CRLF");
                                }
                            }
                        }
                    }
                    originHttpConnection.connect();

                    int originResponseCode = originHttpConnection.getResponseCode();
                    String statusLine = formatted("HTTP/1.1 %s %s\r\n", originResponseCode, originHttpConnection.getResponseMessage());
                    StringBuilder responseHeaders = new StringBuilder();
                    for (var e : originHttpConnection.getHeaderFields().entrySet()) {
                        if (e.getKey() == null) {
                            continue;
                        }
                        responseHeaders.append(e.getKey()).append(": ");
                        for (int i = 0; i < e.getValue().size(); i++) {
                            responseHeaders.append(e.getValue().get(i));
                            if (i < e.getValue().size() - 1) {
                                responseHeaders.append(",");
                            }
                        }
                        responseHeaders.append("\r\n");
                    }
                    responseHeaders.append("\r\n");
                    bos.write(statusLine.getBytes(StandardCharsets.UTF_8));
                    bos.write(responseHeaders.toString().getBytes(StandardCharsets.UTF_8));
                    try (var his = originHttpConnection.getInputStream()) {
                        int originContentLength = originHttpConnection.getContentLength();
                        if (originContentLength > 0) {
                            byte[] bb = new byte[originContentLength];
                            int len = his.readNBytes(bb, 0, originContentLength);
                            bos.write(bb, 0, len);
                        }
                    }
                }
            }

            private static String readLine(InputStream is) throws IOException {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int r = is.read();
                    if (r == -1) {
                        break;
                    }
                    if (r == '\r') {
                        int n = is.read();
                        if (n != '\n') {
                            throw new IllegalStateException("Not CRLF");
                        }
                        break;
                    }
                    sb.append((char) r);
                }
                return sb.toString();
            }

        }).await();
    }

    @AfterClass
    public static void stopHttpServer() throws IOException {
        httpServer.stop(0);
        proxyServer.close();
    }

    @Before
    public void setUpHttpServer() {
        httpServer.createContext("/", new GoogleCloudStorageHttpHandler("bucket"));
        httpServer.createContext("/token", new FakeOAuth2HttpHandler());
    }

    @After
    public void tearDownHttpServer() {
        httpServer.removeContext("/");
        httpServer.removeContext("/token");
    }

    @Override
    protected String repositoryType() {
        return GoogleCloudStorageRepository.TYPE;
    }

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return List.of(GoogleCloudStoragePlugin.class);
    }

    @Override
    protected Settings repositorySettings(String repoName) {
        return Settings.builder()
            .put(super.repositorySettings(repoName))
            .put(BUCKET.getKey(), "bucket")
            .put(CLIENT_NAME.getKey(), "test")
            .build();
    }

    @Override
    protected Settings nodeSettings(int nodeOrdinal, Settings otherSettings) {
        var secureSettings = new MockSecureSettings();
        secureSettings.setFile(
            CREDENTIALS_FILE_SETTING.getConcreteSettingForNamespace("test").getKey(),
            TestUtils.createServiceAccount(random())
        );
        String serverUrl = "http://" + httpServer.getAddress().getHostString() + ":" + httpServer.getAddress().getPort();
        return Settings.builder()
            .put(super.nodeSettings(nodeOrdinal, otherSettings))
            .put(ENDPOINT_SETTING.getConcreteSettingForNamespace("test").getKey(), serverUrl)
            .put(TOKEN_URI_SETTING.getConcreteSettingForNamespace("test").getKey(), serverUrl + "/token")
            .put(PROXY_HOST_SETTING.getConcreteSettingForNamespace("test").getKey(), proxyServer.getHost())
            .put(PROXY_PORT_SETTING.getConcreteSettingForNamespace("test").getKey(), proxyServer.getPort())
            .put(PROXY_TYPE_SETTING.getConcreteSettingForNamespace("test").getKey(), "http")
            .setSecureSettings(secureSettings)
            .build();
    }
}
