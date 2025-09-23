---
layout: default
title: CAS - Google Cloud Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Google Cloud Logging

[Cloud Logging](https://cloud.google.com/logging/) is the managed logging service provided by Google Cloud.

The integration here also provides automatic support for associating a web request trace ID with the corresponding log entries
by retrieving the `X-B3-TraceId` or `X-Cloud-Trace-Context` header values from MDC.
   
## JSON Layout Template

`JsonTemplateLayout` is a customizable, efficient, and garbage-free JSON generating layout. 
It encodes LogEvents according to the structure described by the JSON template provided.      

```xml
<JsonTemplateLayout eventTemplateUri="classpath:GcpLayout.json"/>
```

## CAS

Another option is to use a CAS-provided dedicated logger. Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gcp-logging" %}

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Due to the way logging is set up, the Google Cloud project ID and credentials 
defined in CAS properties are ignored. Instead, you should set the <code>GOOGLE_CLOUD_PROJECT</code> 
and <code>GOOGLE_APPLICATION_CREDENTIALS</code> environment variables to the project ID and credentials 
private key location, where necessary. Alternatively, the Google Cloud project ID can also be set directly
in the logging configuration.</p></div>

This is an example of the logging configuration:

```xml
<Configuration>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <JsonLayout locationInfo="false"
                        includeStacktrace="true"
                        objectMessageAsJsonObject="true"
                        compact="true"
                        properties="false"
                        eventEol="true"
                        includeTimeMillis="false">
                <KeyValuePair key="time" value="$${event:timestamp:-}"/>
                <KeyValuePair key="timestampSeconds" value="$${ctx:timestampSeconds:-}"/>
                <KeyValuePair key="timestampNanos" value="$${ctx:timestampNanos:-}"/>
                <KeyValuePair key="severity" value="$${ctx:severity:-}"/>
                <KeyValuePair key="logging.googleapis.com/insertId" value="$${ctx:insertId:-}"/>
                <KeyValuePair key="logging.googleapis.com/spanId" value="$${ctx:spanId:-}"/>
                <KeyValuePair key="logging.googleapis.com/trace" value="$${ctx:traceId:-}"/>
            </JsonLayout>
        </Console>
        <!-- Update the projectId, or remove and let CAS determine the project id automatically -->
        <GoogleCloudAppender name="GoogleCloudAppender" 
                             flattenMessage="true"
                             projectId="...">
            <AppenderRef ref="casConsole"/>
        </GoogleCloudAppender>
    </Appenders>

    <Loggers>
        <Logger name="org.apereo.cas" includeLocation="true" 
                level="INFO" additivity="false">
            <AppenderRef ref="GoogleCloudAppender"/>
        </Logger>
    </Loggers>

</Configuration>
```

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="gcpLogs" %}

{% include_cached casproperties.html properties="cas.logging.gcp" %}
