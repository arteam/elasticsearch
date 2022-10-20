/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.transform.persistence;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.search.SearchHit;

import java.util.Objects;

/**
 * Simple class to keep track of information needed for optimistic concurrency
 */
public record SeqNoPrimaryTermAndIndex(long seqNo, long primaryTerm, String index) {
    public static SeqNoPrimaryTermAndIndex fromSearchHit(SearchHit hit) {
        return new SeqNoPrimaryTermAndIndex(hit.getSeqNo(), hit.getPrimaryTerm(), hit.getIndex());
    }

    public static SeqNoPrimaryTermAndIndex fromIndexResponse(IndexResponse response) {
        return new SeqNoPrimaryTermAndIndex(response.getSeqNo(), response.getPrimaryTerm(), response.getIndex());
    }
}
