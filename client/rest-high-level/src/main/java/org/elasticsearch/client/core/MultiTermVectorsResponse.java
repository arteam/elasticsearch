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
import org.elasticsearch.xcontent.XContentParser;

import java.util.List;
import java.util.Objects;

import static org.elasticsearch.xcontent.ConstructingObjectParser.constructorArg;

public record MultiTermVectorsResponse(List<TermVectorsResponse> responses) {

    private static final ConstructingObjectParser<MultiTermVectorsResponse, Void> PARSER = new ConstructingObjectParser<>(
        "multi_term_vectors",
        true,
        args -> {
            // as the response comes from server, we are sure that args[0] will be a list of TermVectorsResponse
            @SuppressWarnings("unchecked")
            List<TermVectorsResponse> termVectorsResponsesList = (List<TermVectorsResponse>) args[0];
            return new MultiTermVectorsResponse(termVectorsResponsesList);
        }
    );

    static {
        PARSER.declareObjectArray(constructorArg(), (p, c) -> TermVectorsResponse.fromXContent(p), new ParseField("docs"));
    }

    public static MultiTermVectorsResponse fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    /**
     * Returns the list of {@code TermVectorsResponse} for this {@code MultiTermVectorsResponse}
     */
    public List<TermVectorsResponse> getTermVectorsResponses() {
        return responses;
    }
}
