/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.core.ml.inference.results;

import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.xpack.core.ml.inference.trainedmodel.InferenceConfig.DEFAULT_RESULTS_FIELD;

public record RawInferenceResults(double[] value, double[][] featureImportance) implements InferenceResults {

    public static final String NAME = "raw";

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        throw new UnsupportedOperationException("[raw] does not support wire serialization");
    }

    @Override
    public String getResultsField() {
        return DEFAULT_RESULTS_FIELD;
    }

    @Override
    public Map<String, Object> asMap() {
        throw new UnsupportedOperationException("[raw] does not support map conversion");
    }

    @Override
    public Object predictedValue() {
        return null;
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        throw new UnsupportedOperationException("[raw] does not support toXContent");
    }
}
