/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.ml.dataframe.process.results;

import org.elasticsearch.xcontent.ConstructingObjectParser;
import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.ToXContentObject;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.elasticsearch.xcontent.ConstructingObjectParser.constructorArg;

public record RowResults(int checksum, Map<String, Object> results) implements ToXContentObject {

    public static final ParseField TYPE = new ParseField("row_results");
    public static final ParseField CHECKSUM = new ParseField("checksum");
    public static final ParseField RESULTS = new ParseField("results");

    @SuppressWarnings("unchecked")
    public static final ConstructingObjectParser<RowResults, Void> PARSER = new ConstructingObjectParser<>(
        TYPE.getPreferredName(),
        a -> new RowResults((Integer) a[0], (Map<String, Object>) a[1])
    );

    static {
        PARSER.declareInt(constructorArg(), CHECKSUM);
        PARSER.declareObject(constructorArg(), (p, context) -> p.map(), RESULTS);
    }

    public RowResults {
        Objects.requireNonNull(checksum);
        Objects.requireNonNull(results);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(CHECKSUM.getPreferredName(), checksum);
        builder.field(RESULTS.getPreferredName(), results);
        builder.endObject();
        return builder;
    }

}
