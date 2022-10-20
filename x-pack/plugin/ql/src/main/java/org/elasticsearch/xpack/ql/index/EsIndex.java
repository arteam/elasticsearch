/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.ql.index;

import org.elasticsearch.xpack.ql.type.EsField;

import java.util.Map;
import java.util.Objects;

public record EsIndex(String name, Map<String, EsField> mapping) {

    public EsIndex {
        assert name != null;
        assert mapping != null;
    }

    @Override
    public String toString() {
        return name;
    }
}
