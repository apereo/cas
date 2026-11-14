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

No settings in the configuration catalog match this section.
