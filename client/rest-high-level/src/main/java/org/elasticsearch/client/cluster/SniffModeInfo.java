/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.client.cluster;

import java.util.List;
import java.util.Objects;

public record SniffModeInfo(List<String> seedNodes, int maxConnectionsPerCluster, int numNodesConnected)
    implements
        RemoteConnectionInfo.ModeInfo {

    public static final String NAME = "sniff";
    static final String SEEDS = "seeds";
    static final String NUM_NODES_CONNECTED = "num_nodes_connected";
    static final String MAX_CONNECTIONS_PER_CLUSTER = "max_connections_per_cluster";

    @Override
    public boolean isConnected() {
        return numNodesConnected > 0;
    }

    @Override
    public String modeName() {
        return NAME;
    }
}
