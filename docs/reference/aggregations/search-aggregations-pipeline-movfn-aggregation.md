---
navigation_title: "Moving function"
mapped_pages:
  - https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-pipeline-movfn-aggregation.html
---

# Moving function aggregation [search-aggregations-pipeline-movfn-aggregation]


Given an ordered series of data, the Moving Function aggregation will slide a window across the data and allow the user to specify a custom script that is executed on each window of data. For convenience, a number of common functions are predefined such as min/max, moving averages, etc.

## Syntax [_syntax_18]

A `moving_fn` aggregation looks like this in isolation:

```js
{
  "moving_fn": {
    "buckets_path": "the_sum",
    "window": 10,
    "script": "MovingFunctions.min(values)"
  }
}
```

$$$moving-fn-params$$$

| Parameter Name | Description | Required | Default Value |
| --- | --- | --- | --- |
| `buckets_path` | Path to the metric of interest (see [`buckets_path` Syntax](/reference/aggregations/pipeline.md#buckets-path-syntax) for more details | Required |  |
| `window` | The size of window to "slide" across the histogram. | Required |  |
| `script` | The script that should be executed on each window of data | Required |  |
| `gap_policy` | The policy to apply when gaps are found in the data. See [Dealing with gaps in the data](/reference/aggregations/pipeline.md#gap-policy). | Optional | `skip` |
| `shift` | [Shift](#shift-parameter) of window position. | Optional | 0 |

`moving_fn` aggregations must be embedded inside of a `histogram` or `date_histogram` aggregation. They can be embedded like any other metric aggregation:

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {                  <1>
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }   <2>
        },
        "the_movfn": {
          "moving_fn": {
            "buckets_path": "the_sum",  <3>
            "window": 10,
            "script": "MovingFunctions.unweightedAvg(values)"
          }
        }
      }
    }
  }
}
```

1. A `date_histogram` named "my_date_histo" is constructed on the "timestamp" field, with one-month intervals
2. A `sum` metric is used to calculate the sum of a field. This could be any numeric metric (sum, min, max, etc)
3. Finally, we specify a `moving_fn` aggregation which uses "the_sum" metric as its input.


Moving averages are built by first specifying a `histogram` or `date_histogram` over a field. You can then optionally add numeric metrics, such as a `sum`, inside of that histogram. Finally, the `moving_fn` is embedded inside the histogram. The `buckets_path` parameter is then used to "point" at one of the sibling metrics inside of the histogram (see [`buckets_path` Syntax](/reference/aggregations/pipeline.md#buckets-path-syntax) for a description of the syntax for `buckets_path`.

An example response from the above aggregation may look like:

```console-result
{
   "took": 11,
   "timed_out": false,
   "_shards": ...,
   "hits": ...,
   "aggregations": {
      "my_date_histo": {
         "buckets": [
             {
                 "key_as_string": "2015/01/01 00:00:00",
                 "key": 1420070400000,
                 "doc_count": 3,
                 "the_sum": {
                    "value": 550.0
                 },
                 "the_movfn": {
                    "value": null
                 }
             },
             {
                 "key_as_string": "2015/02/01 00:00:00",
                 "key": 1422748800000,
                 "doc_count": 2,
                 "the_sum": {
                    "value": 60.0
                 },
                 "the_movfn": {
                    "value": 550.0
                 }
             },
             {
                 "key_as_string": "2015/03/01 00:00:00",
                 "key": 1425168000000,
                 "doc_count": 2,
                 "the_sum": {
                    "value": 375.0
                 },
                 "the_movfn": {
                    "value": 305.0
                 }
             }
         ]
      }
   }
}
```


## Custom user scripting [_custom_user_scripting]

The Moving Function aggregation allows the user to specify any arbitrary script to define custom logic. The script is invoked each time a new window of data is collected. These values are provided to the script in the `values` variable. The script should then perform some kind of calculation and emit a single `double` as the result. Emitting `null` is not permitted, although `NaN` and +/- `Inf` are allowed.

For example, this script will simply return the first value from the window, or `NaN` if no values are available:

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_movavg": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "return values.length > 0 ? values[0] : Double.NaN"
          }
        }
      }
    }
  }
}
```


## shift parameter [shift-parameter]

By default (with `shift = 0`), the window that is offered for calculation is the last `n` values excluding the current bucket. Increasing `shift` by 1 moves starting window position by `1` to the right.

* To include current bucket to the window, use `shift = 1`.
* For center alignment (`n / 2` values before and after the current bucket), use `shift = window / 2`.
* For right alignment (`n` values after the current bucket), use `shift = window`.

If either of window edges moves outside the borders of data series, the window shrinks to include available values only.


## Pre-built Functions [_pre_built_functions]

For convenience, a number of functions have been prebuilt and are available inside the `moving_fn` script context:

* `max()`
* `min()`
* `sum()`
* `stdDev()`
* `unweightedAvg()`
* `linearWeightedAvg()`
* `ewma()`
* `holt()`
* `holtWinters()`

The functions are available from the `MovingFunctions` namespace. E.g. `MovingFunctions.max()`

### max Function [_max_function]

This function accepts a collection of doubles and returns the maximum value in that window. `null` and `NaN` values are ignored; the maximum is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `NaN` is returned as the result.

$$$max-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the maximum |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_moving_max": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.max(values)"
          }
        }
      }
    }
  }
}
```


### min Function [_min_function]

This function accepts a collection of doubles and returns the minimum value in that window.  `null` and `NaN` values are ignored; the minimum is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `NaN` is returned as the result.

$$$min-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the minimum |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_moving_min": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.min(values)"
          }
        }
      }
    }
  }
}
```


### sum Function [_sum_function]

This function accepts a collection of doubles and returns the sum of the values in that window.  `null` and `NaN` values are ignored; the sum is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `0.0` is returned as the result.

$$$sum-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the sum of |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_moving_sum": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.sum(values)"
          }
        }
      }
    }
  }
}
```


### stdDev Function [_stddev_function]

This function accepts a collection of doubles and average, then returns the standard deviation of the values in that window. `null` and `NaN` values are ignored; the sum is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `0.0` is returned as the result.

$$$stddev-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the standard deviation of |
| `avg` | The average of the window |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_moving_sum": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.stdDev(values, MovingFunctions.unweightedAvg(values))"
          }
        }
      }
    }
  }
}
```

The `avg` parameter must be provided to the standard deviation function because different styles of averages can be computed on the window (simple, linearly weighted, etc). The various moving averages that are detailed below can be used to calculate the average for the standard deviation function.


### unweightedAvg Function [_unweightedavg_function]

The `unweightedAvg` function calculates the sum of all values in the window, then divides by the size of the window. It is effectively a simple arithmetic mean of the window. The simple moving average does not perform any time-dependent weighting, which means the values from a `simple` moving average tend to "lag" behind the real data.

`null` and `NaN` values are ignored; the average is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `NaN` is returned as the result. This means that the count used in the average calculation is count of non-`null`,non-`NaN` values.

$$$unweightedavg-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the sum of |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_movavg": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.unweightedAvg(values)"
          }
        }
      }
    }
  }
}
```



## linearWeightedAvg Function [_linearweightedavg_function]

The `linearWeightedAvg` function assigns a linear weighting to points in the series, such that "older" datapoints (e.g. those at the beginning of the window) contribute a linearly less amount to the total average. The linear weighting helps reduce the "lag" behind the data’s mean, since older points have less influence.

If the window is empty, or all values are `null`/`NaN`, `NaN` is returned as the result.

$$$linearweightedavg-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the sum of |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_movavg": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.linearWeightedAvg(values)"
          }
        }
      }
    }
  }
}
```


## ewma Function [_ewma_function]

The `ewma` function (aka "single-exponential") is similar to the `linearMovAvg` function, except older data-points become exponentially less important, rather than linearly less important. The speed at which the importance decays can be controlled with an `alpha` setting. Small values make the weight decay slowly, which provides greater smoothing and takes into account a larger portion of the window. Larger values make the weight decay quickly, which reduces the impact of older values on the moving average. This tends to make the moving average track the data more closely but with less smoothing.

`null` and `NaN` values are ignored; the average is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `NaN` is returned as the result. This means that the count used in the average calculation is count of non-`null`,non-`NaN` values.

$$$ewma-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the sum of |
| `alpha` | Exponential decay |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_movavg": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.ewma(values, 0.3)"
          }
        }
      }
    }
  }
}
```


## holt Function [_holt_function]

The `holt` function (aka "double exponential") incorporates a second exponential term which tracks the data’s trend. Single exponential does not perform well when the data has an underlying linear trend. The double exponential model calculates two values internally: a "level" and a "trend".

The level calculation is similar to `ewma`, and is an exponentially weighted view of the data. The difference is that the previously smoothed value is used instead of the raw value, which allows it to stay close to the original series. The trend calculation looks at the difference between the current and last value (e.g. the slope, or trend, of the smoothed data). The trend value is also exponentially weighted.

Values are produced by multiplying the level and trend components.

`null` and `NaN` values are ignored; the average is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `NaN` is returned as the result. This means that the count used in the average calculation is count of non-`null`,non-`NaN` values.

$$$holt-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the sum of |
| `alpha` | Level decay value |
| `beta` | Trend decay value |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_movavg": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "MovingFunctions.holt(values, 0.3, 0.1)"
          }
        }
      }
    }
  }
}
```

In practice, the `alpha` value behaves very similarly in `holtMovAvg` as `ewmaMovAvg`: small values produce more smoothing and more lag, while larger values produce closer tracking and less lag. The value of `beta` is often difficult to see. Small values emphasize long-term trends (such as a constant linear trend in the whole series), while larger values emphasize short-term trends.


## holtWinters Function [_holtwinters_function]

The `holtWinters` function (aka "triple exponential") incorporates a third exponential term which tracks the seasonal aspect of your data. This aggregation therefore smooths based on three components: "level", "trend" and "seasonality".

The level and trend calculation is identical to `holt` The seasonal calculation looks at the difference between the current point, and the point one period earlier.

Holt-Winters requires a little more handholding than the other moving averages. You need to specify the "periodicity" of your data: e.g. if your data has cyclic trends every 7 days, you would set `period = 7`. Similarly if there was a monthly trend, you would set it to `30`. There is currently no periodicity detection, although that is planned for future enhancements.

`null` and `NaN` values are ignored; the average is only calculated over the real values. If the window is empty, or all values are `null`/`NaN`, `NaN` is returned as the result. This means that the count used in the average calculation is count of non-`null`,non-`NaN` values.

$$$holtwinters-params$$$

| Parameter Name | Description |
| --- | --- |
| `values` | The window of values to find the sum of |
| `alpha` | Level decay value |
| `beta` | Trend decay value |
| `gamma` | Seasonality decay value |
| `period` | The periodicity of the data |
| `multiplicative` | True if you wish to use multiplicative holt-winters, false to use additive |

```console
POST /_search
{
  "size": 0,
  "aggs": {
    "my_date_histo": {
      "date_histogram": {
        "field": "date",
        "calendar_interval": "1M"
      },
      "aggs": {
        "the_sum": {
          "sum": { "field": "price" }
        },
        "the_movavg": {
          "moving_fn": {
            "buckets_path": "the_sum",
            "window": 10,
            "script": "if (values.length > 5*2) {MovingFunctions.holtWinters(values, 0.3, 0.1, 0.1, 5, false)}"
          }
        }
      }
    }
  }
}
```

::::{warning}
Multiplicative Holt-Winters works by dividing each data point by the seasonal value. This is problematic if any of your data is zero, or if there are gaps in the data (since this results in a divid-by-zero). To combat this, the `mult` Holt-Winters pads all values by a very small amount (1*10-10) so that all values are non-zero. This affects the result, but only minimally. If your data is non-zero, or you prefer to see `NaN` when zero’s are encountered, you can disable this behavior with `pad: false`

::::


### "Cold Start" [_cold_start]

Unfortunately, due to the nature of Holt-Winters, it requires two periods of data to "bootstrap" the algorithm. This means that your `window` must always be **at least** twice the size of your period. An exception will be thrown if it isn’t. It also means that Holt-Winters will not emit a value for the first `2 * period` buckets; the current algorithm does not backcast.

You’ll notice in the above example we have an `if ()` statement checking the size of values. This is checking to make sure we have two periods worth of data (`5 * 2`, where 5 is the period specified in the `holtWintersMovAvg` function) before calling the holt-winters function.



