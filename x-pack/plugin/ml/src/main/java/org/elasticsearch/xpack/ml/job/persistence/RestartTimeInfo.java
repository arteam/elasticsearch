/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.ml.job.persistence;

import org.elasticsearch.core.Nullable;

public record RestartTimeInfo(@Nullable Long latestFinalBucketTimeMs, @Nullable Long latestRecordTimeMs, boolean haveSeenDataPreviously) {}
