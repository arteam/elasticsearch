---
mapped_pages:
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-field-meta.html
---

# meta [mapping-field-meta]

Metadata attached to the field. This metadata is opaque to Elasticsearch, it is only useful for multiple applications that work on the same indices to share meta information about fields such as units

```console
PUT my-index-000001
{
  "mappings": {
    "properties": {
      "latency": {
        "type": "long",
        "meta": {
          "unit": "ms"
        }
      }
    }
  }
}
```

::::{note}
Field metadata enforces at most 5 entries, that keys have a length that is less than or equal to 20, and that values are strings whose length is less than or equal to 50.
::::


::::{note}
Field metadata is updatable by submitting a mapping update. The metadata of the update will override the metadata of the existing field.
::::


::::{note}
Field metadata is not supported on object or nested fields.
::::


Elastic products use the following standard metadata entries for fields. You can follow these same metadata conventions to get a better out-of-the-box experience with your data.

unit
:   The unit associated with a numeric field: `"percent"`, `"byte"` or a [time unit](/reference/elasticsearch/rest-apis/api-conventions.md#time-units). By default, a field does not have a unit. Only valid for numeric fields. The convention for percents is to use value `1` to mean `100%`.

metric_type
:   The metric type of a numeric field: `"gauge"` or `"counter"`. A gauge is a single-value measurement that can go up or down over time, such as a temperature. A counter is a single-value cumulative counter that only goes up, such as the number of requests processed by a web server, or resets to 0 (zero). By default, no metric type is associated with a field. Only valid for numeric fields.

