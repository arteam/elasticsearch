/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.core.indexing;

import org.elasticsearch.action.index.IndexRequest;

import java.util.stream.Stream;

/**
 * Result object to hold the result of 1 iteration of iterative indexing.
 * Acts as an interface between the implementation and the generic indexer.
 *
 * @param toIndex  the stream of requests to be indexed
 * @param position the extracted, persistable position of the job required for the search phase
 * @param isDone   true if source is exhausted and job should go to sleep
 * Note: toIndex.empty() != isDone due to possible filtering in the specific implementation
 */
public record IterationResult<JobPosition> (Stream<IndexRequest> toIndex, JobPosition position, boolean isDone) {}
