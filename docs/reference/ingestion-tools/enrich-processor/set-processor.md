---
navigation_title: "Set"
mapped_pages:
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/set-processor.html
---

# Set processor [set-processor]


Sets one field and associates it with the specified value. If the field already exists, its value will be replaced with the provided one.

$$$set-options$$$

| Name | Required | Default | Description |
| --- | --- | --- | --- |
| `field` | yes | - | The field to insert, upsert, or update. Supports [template snippets](docs-content://manage-data/ingest/transform-enrich/ingest-pipelines.md#template-snippets). |
| `value` | yes* | - | The value to be set for the field. Supports [template snippets](docs-content://manage-data/ingest/transform-enrich/ingest-pipelines.md#template-snippets). May specify only one of `value` or `copy_from`. |
| `copy_from` | no | - | The origin field which will be copied to `field`, cannot set `value` simultaneously. Supported data types are `boolean`, `number`, `array`, `object`, `string`, `date`, etc. |
| `override` | no | `true` | If `true` processor will update fields with pre-existing non-null-valued field. When set to `false`, such fields will not be touched. |
| `ignore_empty_value` | no | `false` | If `true` and used in combination with `value` which is a [template snippet](docs-content://manage-data/ingest/transform-enrich/ingest-pipelines.md#template-snippets) that evaluates to `null` or an empty string, the processor quietly exits without modifying the document. Similarly, if used in combination with `copy_from` it will quietly exit if the field does not exist or its value evaluates to `null` or an empty string. |
| `media_type` | no | `application/json` | The media type for encoding `value`. Applies only when `value` is a [template snippet](docs-content://manage-data/ingest/transform-enrich/ingest-pipelines.md#template-snippets). Must be one of `application/json`, `text/plain`, or `application/x-www-form-urlencoded`. |
| `description` | no | - | Description of the processor. Useful for describing the purpose of the processor or its configuration. |
| `if` | no | - | Conditionally execute the processor. See [Conditionally run a processor](docs-content://manage-data/ingest/transform-enrich/ingest-pipelines.md#conditionally-run-processor). |
| `ignore_failure` | no | `false` | Ignore failures for the processor. See [Handling pipeline failures](docs-content://manage-data/ingest/transform-enrich/ingest-pipelines.md#handling-pipeline-failures). |
| `on_failure` | no | - | Handle failures for the processor. See [Handling pipeline failures](docs-content://manage-data/ingest/transform-enrich/ingest-pipelines.md#handling-pipeline-failures). |
| `tag` | no | - | Identifier for the processor. Useful for debugging and metrics. |

```js
{
  "description" : "sets the value of count to 1",
  "set": {
    "field": "count",
    "value": 1
  }
}
```

This processor can also be used to copy data from one field to another. For example:

```console
PUT _ingest/pipeline/set_os
{
  "description": "sets the value of host.os.name from the field os",
  "processors": [
    {
      "set": {
        "field": "host.os.name",
        "value": "{{{os}}}"
      }
    }
  ]
}

POST _ingest/pipeline/set_os/_simulate
{
  "docs": [
    {
      "_source": {
        "os": "Ubuntu"
      }
    }
  ]
}
```

Result:

```console-result
{
  "docs" : [
    {
      "doc" : {
        "_index" : "_index",
        "_id" : "_id",
        "_version" : "-3",
        "_source" : {
          "host" : {
            "os" : {
              "name" : "Ubuntu"
            }
          },
          "os" : "Ubuntu"
        },
        "_ingest" : {
          "timestamp" : "2019-03-11T21:54:37.909224Z"
        }
      }
    }
  ]
}
```

This processor can also access array fields using dot notation:

```console
POST /_ingest/pipeline/_simulate
{
  "pipeline": {
    "processors": [
      {
        "set": {
          "field": "my_field",
          "value": "{{{input_field.1}}}"
        }
      }
    ]
  },
  "docs": [
    {
      "_index": "index",
      "_id": "id",
      "_source": {
        "input_field": [
          "Ubuntu",
          "Windows",
          "Ventura"
        ]
      }
    }
  ]
}
```

Result:

```console-result
{
  "docs": [
    {
      "doc": {
        "_index": "index",
        "_id": "id",
        "_version": "-3",
        "_source": {
          "input_field": [
            "Ubuntu",
            "Windows",
            "Ventura"
          ],
          "my_field": "Windows"
        },
        "_ingest": {
          "timestamp": "2023-05-05T16:04:16.456475214Z"
        }
      }
    }
  ]
}
```

The contents of a field including complex values such as arrays and objects can be copied to another field using `copy_from`:

```console
PUT _ingest/pipeline/set_bar
{
  "description": "sets the value of bar from the field foo",
  "processors": [
    {
      "set": {
        "field": "bar",
        "copy_from": "foo"
      }
    }
  ]
}

POST _ingest/pipeline/set_bar/_simulate
{
  "docs": [
    {
      "_source": {
        "foo": ["foo1", "foo2"]
      }
    }
  ]
}
```

Result:

```console-result
{
  "docs" : [
    {
      "doc" : {
        "_index" : "_index",
        "_id" : "_id",
        "_version" : "-3",
        "_source" : {
          "bar": ["foo1", "foo2"],
          "foo": ["foo1", "foo2"]
        },
        "_ingest" : {
          "timestamp" : "2020-09-30T12:55:17.742795Z"
        }
      }
    }
  ]
}
```

