---
layout: default
title: CAS - Logback Configuration
category: Logs & Audits
---

{% include variables.html %}

# Logback Logging

CAS does also support [Logback](https://logback.qos.ch/) as an alternative logging engine. At a high level, 
the Logback architecture is similar to that of [Log4j](Logging.html) where you have `Logger`, `Appender` 
and `Layout` components typically defined inside a `logback.xml` file.

Refer to the [Logback documentation](https://logback.qos.ch/documentation.html) to learn more.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-logback" %}

You must also make sure the following modules and dependencies are excluded from the WAR overlay:

```groovy
configurations.all {
    exclude(group: "org.apache.logging.log4j", module: "log4j-api")
    exclude(group: "org.apache.logging.log4j", module: "log4j-jakarta-web")
    exclude(group: "org.apache.logging.log4j", module: "log4j-web")
    exclude(group: "org.apache.logging.log4j", module: "log4j-jcl")
    exclude(group: "org.apache.logging.log4j", module: "log4j-slf4j-impl")
    exclude(group: "org.apache.logging.log4j", module: "log4j-slf4j2-impl")
    
    exclude(group: "org.apereo.cas", module: "cas-server-core-logging")
}
```

A sample `logback.xml` file follows:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>%white(%d{yyyy-MM-dd HH:mm:ss}) %highlight(%-5level) %cyan(%logger{15}) - %msg%n</Pattern>
        </layout>
    </appender>
    <logger name="org.apereo.cas" level="info" additivity="false">
        <appender-ref ref="console" />
    </logger>
    <root level="info">
        <appender-ref ref="console" />
    </root>
</configuration>
```

<div class="alert alert-warning">:warning: <strong>Be Careful</strong><p>
Sanitizing log data to remove sensitive ticket ids such as ticket-granting tickets or proxy-granting tickets is not handled by CAS when Logback is used. While this 
may be worked out in future releases, you should be extra careful to cleanse log data prior to sharing it with external systems such as Splunk or Syslog, etc. 
</p></div>

{% include_cached casproperties.html thirdPartyStartsWith="logging." %}
