/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.eql.execution.search;

import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * @param implicitTiebreaker _shard_doc tiebreaker automatically added by ES PIT
 */
public record Ordinal(Timestamp timestamp, Comparable<Object> tiebreaker, long implicitTiebreaker)
    implements
        Comparable<Ordinal>,
        Accountable {

    private static final long SHALLOW_SIZE = RamUsageEstimator.shallowSizeOfInstance(Ordinal.class);

    @Override
    public long ramBytesUsed() {
        return SHALLOW_SIZE;
    }

    @Override
    public int compareTo(Ordinal o) {
        int timestampCompare = timestamp.compareTo(o.timestamp);
        if (timestampCompare < 0) {
            return -1;
        }
        if (timestampCompare == 0) {
            if (tiebreaker != null) {
                if (o.tiebreaker != null) {
                    int tiebreakerCompare = tiebreaker.compareTo(o.tiebreaker);
                    return tiebreakerCompare == 0 ? Long.compare(implicitTiebreaker, o.implicitTiebreaker) : tiebreakerCompare;
                } else {
                    return -1;
                }
            }
            // this tiebreaker is null
            else {
                // nulls are last so unless both are null (equal)
                // this ordinal is greater (after) then the other tiebreaker
                // so fall through to 1
                if (o.tiebreaker == null) {
                    return Long.compare(implicitTiebreaker, o.implicitTiebreaker);
                }
            }
        }
        // if none of the branches above matched, this ordinal is greater than o
        return 1;
    }

    public boolean between(Ordinal left, Ordinal right) {
        return (compareTo(left) <= 0 && compareTo(right) >= 0) || (compareTo(right) <= 0 && compareTo(left) >= 0);
    }

    public boolean before(Ordinal other) {
        return compareTo(other) < 0;
    }

    public boolean beforeOrAt(Ordinal other) {
        return compareTo(other) <= 0;
    }

    public boolean after(Ordinal other) {
        return compareTo(other) > 0;
    }

    public boolean afterOrAt(Ordinal other) {
        return compareTo(other) >= 0;
    }

    public Object[] toArray() {
        return tiebreaker != null
            ? new Object[] { timestamp.toString(), tiebreaker, implicitTiebreaker }
            : new Object[] { timestamp.toString(), implicitTiebreaker };
    }
}
