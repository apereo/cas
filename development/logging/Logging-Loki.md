# Grafana Loki Logging

Source: https://apereo.github.io/cas/development/logging/Logging-Loki.html

Log data can be automatically routed to [Grafana Loki](https://grafana.com/oss/loki/). Grafana Loki is a horizontally scalable, highly available, multi-tenant log aggregation system inspired by Prometheus. It is designed to be very cost effective and easy to operate. It does not index the contents of the logs, but rather a set of labels for each log stream.

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-logging-config-loki"
}
```

With the above module, you may then declare a specific appender to communicate with Loki:

```xml
<Appenders>
    <Loki name="LokiAppender">
        <host>loki.mydomain.com</host>
        <port>3100</port>
        <useSSL>false</useSSL>
        <username>...</username>
        <password>...</password>
    
        <PatternLayout>
            <Pattern>%X{tid} [%t] %d{MM-dd HH:mm:ss.SSS} %5p %c{1} - %m%n%exception{full}</Pattern>
        </PatternLayout>
        <ThresholdFilter level="ALL"/>
        <Header name="X-Scope-OrgID" value="Apereo"/>
        <Label name="server" value="cas"/>
        <LogLevelLabel>log_level</LogLevelLabel>
    </Loki>
</Appenders>
...
<Loggers>
    <Root level="error">
        <AppenderRef ref="LokiAppender"/>
    </Root>
</Loggers>
```

The plugin by default sends `POST` requests to `/loki/api/v1/push` HTTP endpoint. The above configuration will configure the appender to call to URL: `http://loki.mydomain.com:3100/loki/api/v1/push`.
