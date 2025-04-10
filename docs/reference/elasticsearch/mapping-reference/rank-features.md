---
navigation_title: "Rank features"
mapped_pages:
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/rank-features.html
---

# Rank features field type [rank-features]


A `rank_features` field can index numeric feature vectors, so that they can later be used to boost documents in queries with a [`rank_feature`](/reference/query-languages/query-dsl/query-dsl-rank-feature-query.md) query.

It is analogous to the [`rank_feature`](/reference/elasticsearch/mapping-reference/rank-feature.md) data type but is better suited when the list of features is sparse so that it wouldn’t be reasonable to add one field to the mappings for each of them.

```console
PUT my-index-000001
{
  "mappings": {
    "properties": {
      "topics": {
        "type": "rank_features" <1>
      },
      "negative_reviews" : {
        "type": "rank_features",
        "positive_score_impact": false <2>
      }
    }
  }
}

PUT my-index-000001/_doc/1
{
  "topics": { <3>
    "politics": 20,
    "economics": 50.8
  },
  "negative_reviews": {
    "1star": 10,
    "2star": 100
  }
}

PUT my-index-000001/_doc/2
{
  "topics": {
    "politics": 5.2,
    "sports": 80.1
  },
  "negative_reviews": {
    "1star": 1,
    "2star": 10
  }
}

GET my-index-000001/_search
{
  "query": { <4>
    "rank_feature": {
      "field": "topics.politics"
    }
  }
}

GET my-index-000001/_search
{
  "query": { <5>
    "rank_feature": {
      "field": "negative_reviews.1star"
    }
  }
}

GET my-index-000001/_search
{
  "query": { <6>
    "term": {
      "topics": "economics"
    }
  }
}
```

1. Rank features fields must use the `rank_features` field type
2. Rank features that correlate negatively with the score need to declare it
3. Rank features fields must be a hash with string keys and strictly positive numeric values
4. This query ranks documents by how much they are about the "politics" topic.
5. This query ranks documents inversely to the number of "1star" reviews they received.
6. This query returns documents that store the "economics" feature in the "topics" field.


::::{note}
`rank_features` fields only support single-valued features and strictly positive values. Multi-valued fields and zero or negative values will be rejected.
::::


::::{note}
`rank_features` fields do not support sorting or aggregating and may only be queried using [`rank_feature`](/reference/query-languages/query-dsl/query-dsl-rank-feature-query.md) or [`term`](/reference/query-languages/query-dsl/query-dsl-term-query.md) queries.
::::


::::{note}
[`term`](/reference/query-languages/query-dsl/query-dsl-term-query.md) queries on `rank_features` fields are scored by multiplying the matched stored feature value by the provided `boost`.
::::


::::{note}
`rank_features` fields only preserve 9 significant bits for the precision, which translates to a relative error of about 0.4%.
::::


Rank features that correlate negatively with the score should set `positive_score_impact` to `false` (defaults to `true`). This will be used by the [`rank_feature`](/reference/query-languages/query-dsl/query-dsl-rank-feature-query.md) query to modify the scoring formula in such a way that the score decreases with the value of the feature instead of increasing.

