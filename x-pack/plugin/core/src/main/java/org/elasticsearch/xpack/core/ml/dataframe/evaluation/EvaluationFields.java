/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.core.ml.dataframe.evaluation;

import org.elasticsearch.core.Tuple;
import org.elasticsearch.xcontent.ParseField;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates fields needed by evaluation.
 *
 * @param actualField                     The field containing the actual value
 * @param predictedField                  The field containing the predicted value
 * @param topClassesField                 The field containing the array of top classes
 * @param predictedClassField             The field containing the predicted class name value
 * @param predictedProbabilityField       The field containing the predicted probability value in [0.0, 1.0]
 * @param predictedProbabilityFieldNested Whether the {@code predictedProbabilityField} should be treated as nested (e.g.: when used in exists queries).
 */
public record EvaluationFields(
    String actualField,
    String predictedField,
    String topClassesField,
    String predictedClassField,
    String predictedProbabilityField,
    boolean predictedProbabilityFieldNested
) {

    public static final ParseField ACTUAL_FIELD = new ParseField("actual_field");
    public static final ParseField PREDICTED_FIELD = new ParseField("predicted_field");
    public static final ParseField TOP_CLASSES_FIELD = new ParseField("top_classes_field");
    public static final ParseField PREDICTED_CLASS_FIELD = new ParseField("predicted_class_field");
    public static final ParseField PREDICTED_PROBABILITY_FIELD = new ParseField("predicted_probability_field");

    /**
     * Returns whether the {@code predictedProbabilityField} should be treated as nested (e.g.: when used in exists queries).
     */
    @Override
    public boolean predictedProbabilityFieldNested() {
        return predictedProbabilityFieldNested;
    }

    public List<Tuple<String, String>> listPotentiallyRequiredFields() {
        return Arrays.asList(
            Tuple.tuple(ACTUAL_FIELD.getPreferredName(), actualField),
            Tuple.tuple(PREDICTED_FIELD.getPreferredName(), predictedField),
            Tuple.tuple(TOP_CLASSES_FIELD.getPreferredName(), topClassesField),
            Tuple.tuple(PREDICTED_CLASS_FIELD.getPreferredName(), predictedClassField),
            Tuple.tuple(PREDICTED_PROBABILITY_FIELD.getPreferredName(), predictedProbabilityField)
        );
    }

}
