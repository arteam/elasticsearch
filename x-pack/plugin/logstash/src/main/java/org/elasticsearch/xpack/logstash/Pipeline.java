/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.logstash;

import org.elasticsearch.xcontent.ConstructingObjectParser;
import org.elasticsearch.xcontent.ObjectParser.ValueType;
import org.elasticsearch.xcontent.ParseField;

import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.elasticsearch.xcontent.ConstructingObjectParser.constructorArg;

public record Pipeline(
    String id,
    Instant lastModified,
    Map<String, Object> pipelineMetadata,
    String username,
    String pipeline,
    Map<String, Object> pipelineSettings
) {

    @SuppressWarnings("unchecked")
    public static final ConstructingObjectParser<Pipeline, String> PARSER = new ConstructingObjectParser<>(
        "pipeline",
        true,
        (objects, id) -> {
            Iterator<Object> iterator = Arrays.asList(objects).iterator();
            return new Pipeline(
                id,
                (Instant) iterator.next(),
                (Map<String, Object>) iterator.next(),
                (String) iterator.next(),
                (String) iterator.next(),
                (Map<String, Object>) iterator.next()
            );
        }
    );

    public static final ParseField LAST_MODIFIED = new ParseField("last_modified");
    public static final ParseField PIPELINE_METADATA = new ParseField("pipeline_metadata");
    public static final ParseField USERNAME = new ParseField("username");
    public static final ParseField PIPELINE = new ParseField("pipeline");
    public static final ParseField PIPELINE_SETTINGS = new ParseField("pipeline_settings");

    static {
        PARSER.declareField(constructorArg(), (parser, s) -> {
            final String instantISOString = parser.text();
            return Instant.parse(instantISOString);
        }, LAST_MODIFIED, ValueType.STRING);
        PARSER.declareObject(constructorArg(), (parser, s) -> parser.map(), PIPELINE_METADATA);
        PARSER.declareString(constructorArg(), USERNAME);
        PARSER.declareString(constructorArg(), PIPELINE);
        PARSER.declareObject(constructorArg(), (parser, s) -> parser.map(), PIPELINE_SETTINGS);
    }

}
