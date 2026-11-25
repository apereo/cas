# CAS - Zipkin Distributed Tracing

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Tracing-Zipkin.html

[Brave](https://github.com/openzipkin/brave) is a distributed tracing instrumentation library. Brave typically intercepts production requests to gather timing data, correlate and propagate trace contexts. While typically trace data is sent to Zipkin server or Amazon X-Ray, etc.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-tracing-zipkin"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `management.tracing.export.zipkin.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to Zipkin.
- `management.tracing.export.zipkin.enabled` (Third party; type `Boolean`; default `true`): Whether auto-configuration of tracing is enabled to export Zipkin traces.
- `management.tracing.export.zipkin.encoding` (Third party; type `ZipkinProperties.Encoding`; default `json`): How to encode the POST body to the Zipkin API.
- `management.tracing.export.zipkin.endpoint` (Third party; type `String`; default `http://localhost:9411/api/v2/spans`): URL to the Zipkin API.
- `management.tracing.export.zipkin.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to Zipkin.
