/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.index.fielddata.plain;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedSetSortField;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.core.Nullable;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.IndexFieldData.XFieldComparatorSource.Nested;
import org.elasticsearch.index.fielddata.IndexFieldDataCache;
import org.elasticsearch.index.fielddata.LeafOrdinalsFieldData;
import org.elasticsearch.indices.breaker.CircuitBreakerService;
import org.elasticsearch.script.field.ToScriptFieldFactory;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;
import org.elasticsearch.search.sort.BucketedSort;
import org.elasticsearch.search.sort.SortOrder;

import static org.elasticsearch.index.fielddata.IndexFieldData.XFieldComparatorSource.sortMissingLast;

public class SortedOrdinalsIndexFieldData extends AbstractIndexOrdinalsFieldData {

    public static class Builder implements IndexFieldData.Builder {
        private final String name;
        private final ToScriptFieldFactory<SortedSetDocValues> toScriptFieldFactory;
        private final ValuesSourceType valuesSourceType;

        public Builder(String name, ValuesSourceType valuesSourceType, ToScriptFieldFactory<SortedSetDocValues> toScriptFieldFactory) {
            this.name = name;
            this.toScriptFieldFactory = toScriptFieldFactory;
            this.valuesSourceType = valuesSourceType;
        }

        @Override
        public SortedOrdinalsIndexFieldData build(IndexFieldDataCache cache, CircuitBreakerService breakerService) {
            return new SortedOrdinalsIndexFieldData(cache, name, valuesSourceType, breakerService, toScriptFieldFactory);
        }
    }

    public SortedOrdinalsIndexFieldData(
        IndexFieldDataCache cache,
        String fieldName,
        ValuesSourceType valuesSourceType,
        CircuitBreakerService breakerService,
        ToScriptFieldFactory<SortedSetDocValues> toScriptFieldFactory
    ) {
        super(fieldName, valuesSourceType, cache, breakerService, toScriptFieldFactory);
    }

    @Override
    public SortField sortField(@Nullable Object missingValue, MultiValueMode sortMode, Nested nested, boolean reverse) {
        SortField sortField = new SortField(getFieldName(), SortField.Type.STRING, reverse);
        sortField.setMissingValue(
            sortMissingLast(missingValue) ^ reverse ? SortedSetSortField.STRING_LAST : SortedSetSortField.STRING_FIRST
        );
        return sortField;
    }

    @Override
    public BucketedSort newBucketedSort(
        BigArrays bigArrays,
        Object missingValue,
        MultiValueMode sortMode,
        Nested nested,
        SortOrder sortOrder,
        DocValueFormat format,
        int bucketSize,
        BucketedSort.ExtraData extra
    ) {
        throw new IllegalArgumentException("only supported on numeric fields");
    }

    @Override
    public LeafOrdinalsFieldData load(LeafReaderContext context) {
        // Doc value fields are loaded using Lucene's DocValues#getSortedSet
        // that can happily load SortedDocValues as well.
        return new SortedSetBytesLeafFieldData(context.reader(), getFieldName(), toScriptFieldFactory);
    }

    @Override
    public LeafOrdinalsFieldData loadDirect(LeafReaderContext context) {
        return load(context);
    }

    @Override
    public boolean supportsGlobalOrdinalsMapping() {
        return true;
    }
}
