/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.core.ml.dataframe.evaluation;

/**
 * Encapsulates parameters needed by evaluation.
 *
 * @param maxBuckets Maximum number of buckets allowed in any single search request.
 */
public record EvaluationParameters(int maxBuckets) {

}
