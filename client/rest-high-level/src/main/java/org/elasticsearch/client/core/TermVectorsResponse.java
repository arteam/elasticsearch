/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.client.core;

import org.elasticsearch.core.Nullable;
import org.elasticsearch.xcontent.ConstructingObjectParser;
import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.XContentParser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.elasticsearch.xcontent.ConstructingObjectParser.constructorArg;
import static org.elasticsearch.xcontent.ConstructingObjectParser.optionalConstructorArg;

/**
 * @param index the index for the response
 * @param id the id of the request (can be NULL if there is no document ID)
 * @param docVersion the document version
 * @param found Returns if the document is found, always <code>true</code> for artificial documents
 * @param tookInMillis the time that a request took in milliseconds
 * @param termVectorList the list of term vectors
 */
public record TermVectorsResponse(
    String index,
    String id,
    long docVersion,
    boolean found,
    long tookInMillis,
    List<TermVector> termVectorList
) {

    private static final ConstructingObjectParser<TermVectorsResponse, Void> PARSER = new ConstructingObjectParser<>(
        "term_vectors",
        true,
        args -> {
            // as the response comes from server, we are sure that args[5] will be a list of TermVector
            @SuppressWarnings("unchecked")
            List<TermVector> termVectorList = (List<TermVector>) args[5];
            if (termVectorList != null) {
                Collections.sort(termVectorList, Comparator.comparing(TermVector::fieldName));
            }
            return new TermVectorsResponse(
                (String) args[0],
                (String) args[1],
                (long) args[2],
                (boolean) args[3],
                (long) args[4],
                termVectorList
            );
        }
    );

    static {
        PARSER.declareString(constructorArg(), new ParseField("_index"));
        PARSER.declareString(optionalConstructorArg(), new ParseField("_id"));
        PARSER.declareLong(constructorArg(), new ParseField("_version"));
        PARSER.declareBoolean(constructorArg(), new ParseField("found"));
        PARSER.declareLong(constructorArg(), new ParseField("took"));
        PARSER.declareNamedObjects(
            optionalConstructorArg(),
            (p, c, fieldName) -> TermVector.fromXContent(p, fieldName),
            new ParseField("term_vectors")
        );
    }

    public static TermVectorsResponse fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    /**
     * @param fieldName       field name of the current term vector
     * @param fieldStatistics field statistics for the current field
     * @param terms           list of terms for the current term vector
     */
    public record TermVector(
        String fieldName,
        @Nullable TermVectorsResponse.TermVector.FieldStatistics fieldStatistics,
        @Nullable List<Term> terms
    ) {

        private static final ConstructingObjectParser<TermVector, String> PARSER = new ConstructingObjectParser<>(
            "term_vector",
            true,
            (args, ctxFieldName) -> {
                // as the response comes from server, we are sure that args[1] will be a list of Term
                @SuppressWarnings("unchecked")
                List<Term> terms = (List<Term>) args[1];
                if (terms != null) {
                    Collections.sort(terms, Comparator.comparing(Term::term));
                }
                return new TermVector(ctxFieldName, (FieldStatistics) args[0], terms);
            }
        );

        static {
            PARSER.declareObject(optionalConstructorArg(), (p, c) -> FieldStatistics.fromXContent(p), new ParseField("field_statistics"));
            PARSER.declareNamedObjects(optionalConstructorArg(), (p, c, term) -> Term.fromXContent(p, term), new ParseField("terms"));
        }

        public TermVector(String fieldName, FieldStatistics fieldStatistics, List<Term> terms) {
            this.fieldName = fieldName;
            this.fieldStatistics = fieldStatistics;
            this.terms = terms;
        }

        public static TermVector fromXContent(XContentParser parser, String fieldName) {
            return PARSER.apply(parser, fieldName);
        }

        /**
         * Class containing a general field statistics for the field
         *
         * @param sumDocFreq       the sum of document frequencies for all terms in this field
         * @param sumTotalTermFreq the sum of total term frequencies of all terms in this field
         */
        public record FieldStatistics(long sumDocFreq, int docCount, long sumTotalTermFreq) {

            private static final ConstructingObjectParser<FieldStatistics, Void> PARSER = new ConstructingObjectParser<>(
                "field_statistics",
                true,
                args -> { return new FieldStatistics((long) args[0], (int) args[1], (long) args[2]); }
            );

            static {
                PARSER.declareLong(constructorArg(), new ParseField("sum_doc_freq"));
                PARSER.declareInt(constructorArg(), new ParseField("doc_count"));
                PARSER.declareLong(constructorArg(), new ParseField("sum_ttf"));
            }

            public static FieldStatistics fromXContent(XContentParser parser) {
                return PARSER.apply(parser, null);
            }

        }

        /**
         * @param term          the string representation of the term
         * @param termFreq      term frequency - the number of times this term occurs in the current document
         * @param docFreq       document frequency - the number of documents in the index that contain this term
         * @param totalTermFreq total term frequency - the number of times this term occurs across all documents
         * @param score         tf-idf score, if the request used some form of terms filtering
         * @param tokens        list of tokens for the term
         */
        public record Term(
            String term,
            int termFreq,
            @Nullable Integer docFreq,
            @Nullable Long totalTermFreq,
            @Nullable Float score,
            @Nullable List<Token> tokens
        ) {

            private static final ConstructingObjectParser<Term, String> PARSER = new ConstructingObjectParser<>(
                "token",
                true,
                (args, ctxTerm) -> {
                    // as the response comes from server, we are sure that args[4] will be a list of Token
                    @SuppressWarnings("unchecked")
                    List<Token> tokens = (List<Token>) args[4];
                    if (tokens != null) {
                        Collections.sort(
                            tokens,
                            Comparator.comparing(Token::position, Comparator.nullsFirst(Integer::compareTo))
                                .thenComparing(Token::startOffset, Comparator.nullsFirst(Integer::compareTo))
                                .thenComparing(Token::endOffset, Comparator.nullsFirst(Integer::compareTo))
                        );
                    }
                    return new Term(ctxTerm, (int) args[0], (Integer) args[1], (Long) args[2], (Float) args[3], tokens);
                }
            );

            static {
                PARSER.declareInt(constructorArg(), new ParseField("term_freq"));
                PARSER.declareInt(optionalConstructorArg(), new ParseField("doc_freq"));
                PARSER.declareLong(optionalConstructorArg(), new ParseField("ttf"));
                PARSER.declareFloat(optionalConstructorArg(), new ParseField("score"));
                PARSER.declareObjectArray(optionalConstructorArg(), (p, c) -> Token.fromXContent(p), new ParseField("tokens"));
            }

            public static Term fromXContent(XContentParser parser, String term) {
                return PARSER.apply(parser, term);
            }

        }

        /**
         * @param startOffset the start offset of the token in the document's field
         * @param endOffset   the end offset of the token in the document's field
         * @param position    the position of the token in the document's field
         * @param payload     the payload of the token or <code>null</code> if the payload doesn't exist
         */
        public record Token(
            @Nullable Integer startOffset,
            @Nullable Integer endOffset,
            @Nullable Integer position,
            @Nullable String payload
        ) {

            private static final ConstructingObjectParser<Token, Void> PARSER = new ConstructingObjectParser<>(
                "token",
                true,
                args -> { return new Token((Integer) args[0], (Integer) args[1], (Integer) args[2], (String) args[3]); }
            );

            static {
                PARSER.declareInt(optionalConstructorArg(), new ParseField("start_offset"));
                PARSER.declareInt(optionalConstructorArg(), new ParseField("end_offset"));
                PARSER.declareInt(optionalConstructorArg(), new ParseField("position"));
                PARSER.declareString(optionalConstructorArg(), new ParseField("payload"));
            }

            public Token(Integer startOffset, Integer endOffset, Integer position, String payload) {
                this.startOffset = startOffset;
                this.endOffset = endOffset;
                this.position = position;
                this.payload = payload;
            }

            public static Token fromXContent(XContentParser parser) {
                return PARSER.apply(parser, null);
            }
        }
    }
}
