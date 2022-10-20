/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.client.core;

import org.elasticsearch.xcontent.ConstructingObjectParser;
import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.ToXContentObject;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Paging parameters for GET requests
 * @param from skips the specified number of items. When {@code null} the default value will be used.
 * @param size specifies the maximum number of items to obtain. When {@code null} the default value will be used.
 */
public record PageParams(Integer from, Integer size) implements ToXContentObject {

    public static final ParseField PAGE = new ParseField("page");
    public static final ParseField FROM = new ParseField("from");
    public static final ParseField SIZE = new ParseField("size");

    public static final ConstructingObjectParser<PageParams, Void> PARSER = new ConstructingObjectParser<>(
        PAGE.getPreferredName(),
        a -> new PageParams((Integer) a[0], (Integer) a[1])
    );

    static {
        PARSER.declareInt(ConstructingObjectParser.optionalConstructorArg(), FROM);
        PARSER.declareInt(ConstructingObjectParser.optionalConstructorArg(), SIZE);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (from != null) {
            builder.field(FROM.getPreferredName(), from);
        }
        if (size != null) {
            builder.field(SIZE.getPreferredName(), size);
        }
        builder.endObject();
        return builder;
    }

}
