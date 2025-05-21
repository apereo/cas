---
layout: default
title: CAS - Fluentd Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Fluentd Logging

[Fluentd](https://docs.fluentd.org/) is an open-source data collector for a unified logging layer. 
Fluentd allows you to unify data collection and consumption for better use and understanding of data.

CAS log data can be automatically routed to Fluentd. Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-logging-config-fluentd" %}

With the above module, you may then declare a specific appender to communicate with AWS CloudWatch:

```xml
<Configuration>
    <Appenders>
        <Fluentd name="fluentd" tag="yourTag" >
        <!-- 
          all fields are optional, fields name will be sent to fulentd as a key in json
          Field value/pattern can follow the Pattern as specified in PatternLayout  
          Refer: https://logging.apache.org/log4j/2.x/manual/layouts.html#PatternLayout
        -->
            <Field name="application">CAS</Field>
            <Field name="someOtherField">Otherfield %X{traceId}</Field>
            <Field name="lookupField" pattern="%N"/>   
        <!-- 
          all settings are optional, see FluencyBuilderForFluentd; for default values
          you can add as may fields as you like (or none at all)
        -->
            <FluentdConfig 
              maxBufferSize="536870912"
              bufferChunkInitialSize="1048576"
              bufferChunkRetentionSize="4194304"
              bufferChunkRetentionTimeMillis="1000"
              flushAttemptIntervalMillis="600"
              waitUntilBufferFlushed="10"
              waitUntilFlusherTerminated="10"
              senderMaxRetryCount="8"
              senderBaseRetryIntervalMillis="400"
              senderMaxRetryIntervalMillis="30000"
              connectionTimeoutMillis="5000"
              readTimeoutMillis="5000"
              ackResponseMode="true"
              sslEnabled="false"
              jvmHeapBufferMode="true"
              fileBackupDir="true">
              <!-- 
              all Servers are optional, localhost:24224 will be used if none are specified
              If multiple servers are specified,
                message will be sent to only one of them depending on availability
              --> 
              <Server host="localhost" port="24224" />
              <Server host="127.0.0.1" port="24224" />    
            </FluentdConfig>
        </Fluentd>
    </Appenders>
    
    <Loggers>
        <Logger name="org.apereo" additivity="true" level="trace">
            <appender-ref ref="fluentd" />
        </Logger>
    </Loggers>
</Configuration>
```


