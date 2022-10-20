/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.search.fetch.subphase.highlight;

import org.apache.lucene.search.Query;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.search.fetch.FetchContext;
import org.elasticsearch.search.fetch.FetchSubPhase;

import java.util.Map;

public record FieldHighlightContext(
    String fieldName,
    SearchHighlightContext.Field field,
    MappedFieldType fieldType,
    FetchContext context,
    FetchSubPhase.HitContext hitContext,
    Query query,
    boolean forceSource,
    Map<String, Object> cache
) {

}
