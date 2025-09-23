---
layout: default
title: CAS - Sentry Monitoring Integration
category: Logs & Audits
---

{% include variables.html %}

# Overview

[Sentry](https://sentry.io) allows you to track logs and error in real time. It provides 
insight into production deployments and information to reproduce and fix crashes.
             
The integration here supports error handling and reporting to Sentry, performance monitoring via spans and transactions 
as well as Sentry logging support. 

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sentry" %}

The [Logging](../logging/Logging.html) configuration file must be adjusted to match the following:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <!-- Setting minimumBreadcrumbLevel modifies the default minimum level to add breadcrumbs from INFO to DEBUG  -->
        <!-- Setting minimumEventLevel the default minimum level to capture an event from ERROR to WARN  -->
        <Sentry name="Sentry" 
                minimumBreadcrumbLevel="DEBUG"
                minimumEventLevel="WARN"
                dsn="..." />
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="Sentry"/>
            <AppenderRef ref="casConsole"/>
        </Root>
    </Loggers>
</Configuration>
```

{% include_cached casproperties.html thirdPartyStartsWith="sentry." %}

Breadcrumbs are kept in memory (by default the last `100` records) and are sent with events. For example, by default, 
if you log 100 entries with `logger.info` or `logger.warn`, no event is sent to Sentry. If you then 
log with `logger.error`, an event is sent to Sentry that includes those `100` info or warn messages. 
For this to work, `SentryAppender` needs to receive all log entries to decide what to keep as breadcrumb or send as event. 
Set the `SentryAppender` log level configuration to a value lower than what is set for the `minimumBreadcrumbLevel` and `minimumEventLevel` 
so that it receives these log messages.

Finally, you will need to configure your DSN (client key) and optionally other values such as environment and release. 

You can do so in a `src/main/resources/sentry.properties` file:

```properties
dsn=https://12345@12345.ingest.sentry.io/12345
```
                  
Or via system properties when you launch CAS:

```properties
java -Dsentry.dsn=https://12345@12345.ingest.sentry.io/12345 ...
```

Or via environment variables before you launch CAS:

```bash
export SENTRY_DSN=https://12345@12345.ingest.sentry.io/12345
```
