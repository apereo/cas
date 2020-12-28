### Atlas

By default, metrics are exported to Atlas running on your 
local machine. The location of the Atlas server to use can be provided using:

```properties
# management.metrics.export.atlas.uri=http://atlas.example.com:7101/api/v1/publish
```

### Datadog

Datadog registry pushes metrics to `datadoghq` periodically. To export 
metrics to Datadog, your API key must be provided:

```properties
# management.metrics.export.datadog.api-key=YOUR_KEY
```

You can also change the interval at which metrics are sent to Datadog:

```properties
# management.metrics.export.datadog.step=30s
```

### Ganglia

By default, metrics are exported to Ganglia running on your local 
machine. The Ganglia server host and port to use can be provided using:

```properties
# management.metrics.export.ganglia.host=ganglia.example.com
# management.metrics.export.ganglia.port=9649
```

### Graphite

By default, metrics are exported to Graphite running on your local 
machine. The Graphite server host and port to use can be provided using:

```properties
# management.metrics.export.graphite.host=graphite.example.com
# management.metrics.export.graphite.port=9004
```

### InfluxDb

By default, metrics are exported to Influx running on your local 
machine. The location of the Influx server to use can be provided using:

```properties
# management.metrics.export.influx.uri=http://influx.example.com:8086
```
### JMX

Micrometer provides a hierarchical mapping to JMX, primarily as a 
cheap and portable way to view metrics locally.

### New Relic

New Relic registry pushes metrics to New Relic periodically. To export 
metrics to New Relic, your API key and account id must be provided:

```properties
# management.metrics.export.newrelic.api-key=YOUR_KEY
# management.metrics.export.newrelic.account-id=YOUR_ACCOUNT_ID
```

You can also change the interval at which metrics are sent to New Relic:

```properties
# management.metrics.export.newrelic.step=30s
```

### Prometheus

Prometheus expects to scrape or poll individual app instances for metrics. Spring Boot provides an actuator endpoint
available at `/actuator/prometheus` to present a Prometheus scrape with the appropriate format.

Here is an example `scrape_config` to add to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'spring'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['HOST:PORT']
``` 

### SignalFx

SignalFx registry pushes metrics to SignalFx periodically. To export 
metrics to SignalFx, your access token must be provided:

```properties
# management.metrics.export.signalfx.access-token=YOUR_ACCESS_TOKEN
```

You can also change the interval at which metrics are sent to SignalFx:

```properties
# management.metrics.export.signalfx.step=30s
```

Micrometer ships with a simple, in-memory backend that is automatically 
used as a fallback if no other registry is configured.
This allows you to see what metrics are collected in the metrics endpoint.

The in-memory backend disables itself as soon as you’re using any of 
the other available backend. You can also disable it explicitly:

```properties
# management.metrics.export.simple.enabled=false
```

### StatsD

The StatsD registry pushes metrics over UDP to a StatsD agent eagerly. By default,
metrics are exported to a StatsD agent running on your local machine. 
The StatsD agent host and port to use can be provided using:

```properties
# management.metrics.export.statsd.host=statsd.example.com
# management.metrics.export.statsd.port=9125
```

You can also change the StatsD line protocol to use (default to Datadog):

```properties
# management.metrics.export.statsd.flavor=etsy
```

### Wavefront

Wavefront registry pushes metrics to Wavefront periodically. If you are exporting metrics to
Wavefront directly, your API token must be provided:

```properties
# management.metrics.export.wavefront.api-token=YOUR_API_TOKEN
```

Alternatively, you may use a Wavefront sidecar or an internal proxy set up in your environment that
forwards metrics data to the Wavefront API host:

```properties
# management.metrics.export.uri=proxy://localhost:2878
```

You can also change the interval at which metrics are sent to Wavefront:

```properties
# management.metrics.export.wavefront.step=30s
```
