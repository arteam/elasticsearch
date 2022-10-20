/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.core.ml.dataframe.evaluation;

import org.elasticsearch.core.Tuple;
import org.elasticsearch.test.ESTestCase;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class EvaluationFieldsTests extends ESTestCase {

    public void testConstructorAndGetters() {
        EvaluationFields fields = new EvaluationFields("a", "b", "c", "d", "e", true);
        assertThat(fields.actualField(), is(equalTo("a")));
        assertThat(fields.predictedField(), is(equalTo("b")));
        assertThat(fields.topClassesField(), is(equalTo("c")));
        assertThat(fields.predictedClassField(), is(equalTo("d")));
        assertThat(fields.predictedProbabilityField(), is(equalTo("e")));
        assertThat(fields.predictedProbabilityFieldNested(), is(true));
    }

    public void testConstructorAndGetters_WithNullValues() {
        EvaluationFields fields = new EvaluationFields("a", null, "c", null, "e", true);
        assertThat(fields.actualField(), is(equalTo("a")));
        assertThat(fields.predictedField(), is(nullValue()));
        assertThat(fields.topClassesField(), is(equalTo("c")));
        assertThat(fields.predictedClassField(), is(nullValue()));
        assertThat(fields.predictedProbabilityField(), is(equalTo("e")));
        assertThat(fields.predictedProbabilityFieldNested(), is(true));
    }

    public void testListPotentiallyRequiredFields() {
        EvaluationFields fields = new EvaluationFields("a", "b", "c", "d", "e", randomBoolean());
        assertThat(fields.listPotentiallyRequiredFields().stream().map(Tuple::v2).collect(toList()), contains("a", "b", "c", "d", "e"));
    }

    public void testListPotentiallyRequiredFields_WithNullValues() {
        EvaluationFields fields = new EvaluationFields("a", null, "c", null, "e", randomBoolean());
        assertThat(fields.listPotentiallyRequiredFields().stream().map(Tuple::v2).collect(toList()), contains("a", null, "c", null, "e"));
    }
}
