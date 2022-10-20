/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.searchablesnapshots.cache.common;

import org.elasticsearch.index.shard.ShardId;

import java.util.Objects;

public record CacheKey(String snapshotUUID, String snapshotIndexName, ShardId shardId, String fileName) {

    public CacheKey {
        Objects.requireNonNull(snapshotUUID);
        Objects.requireNonNull(snapshotIndexName);
        Objects.requireNonNull(shardId);
        Objects.requireNonNull(fileName);
    }
}
