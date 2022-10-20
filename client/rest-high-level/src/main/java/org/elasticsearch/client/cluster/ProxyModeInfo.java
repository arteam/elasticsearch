/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.client.cluster;

import java.util.Objects;

public record ProxyModeInfo(String address, String serverName, int maxSocketConnections, int numSocketsConnected)
    implements
        RemoteConnectionInfo.ModeInfo {

    static final String NAME = "proxy";
    static final String PROXY_ADDRESS = "proxy_address";
    static final String SERVER_NAME = "server_name";
    static final String NUM_PROXY_SOCKETS_CONNECTED = "num_proxy_sockets_connected";
    static final String MAX_PROXY_SOCKET_CONNECTIONS = "max_proxy_socket_connections";

    @Override
    public boolean isConnected() {
        return numSocketsConnected > 0;
    }

    @Override
    public String modeName() {
        return NAME;
    }

}
