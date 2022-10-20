/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.monitoring.exporter.http;

import org.apache.http.client.config.RequestConfig.Builder;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.core.Nullable;
import org.elasticsearch.core.TimeValue;

/**
 * {@code TimeoutRequestConfigCallback} enables the setting of connection-related timeouts for HTTP requests.
 */
record TimeoutRequestConfigCallback(@Nullable TimeValue connectTimeout, @Nullable TimeValue socketTimeout)
    implements
        RestClientBuilder.RequestConfigCallback {

    /**
     * Create a new {@link TimeoutRequestConfigCallback}.
     *
     * @param connectTimeout The initial connection timeout, if any is supplied
     * @param socketTimeout  The socket timeout, if any is supplied
     */
    TimeoutRequestConfigCallback {
        assert connectTimeout != null || socketTimeout != null : "pointless to use with defaults";
    }

    /**
     * Sets the {@linkplain Builder#setConnectTimeout(int) connect timeout} and {@linkplain Builder#setSocketTimeout(int) socket timeout}.
     *
     * @param requestConfigBuilder The request to configure.
     * @return Always {@code requestConfigBuilder}.
     */
    @Override
    public Builder customizeRequestConfig(Builder requestConfigBuilder) {
        if (connectTimeout != null) {
            requestConfigBuilder.setConnectTimeout((int) connectTimeout.millis());
        }
        if (socketTimeout != null) {
            requestConfigBuilder.setSocketTimeout((int) socketTimeout.millis());
        }

        return requestConfigBuilder;
    }

}
