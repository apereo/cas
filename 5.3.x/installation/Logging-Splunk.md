---
layout: default
title: CAS - Splunk Logging Configuration
---

# Splunk Logging

Log data can be automatically routed to [Splunk](https://splunk.com/). Support is enabled by including the following module in the overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-logging-config-splunk</artifactId>
     <version>${cas.version}</version>
</dependency>
```

With the above module, you may then declare a specific appender to communicate with Splunk. 
Following is an example that assumes that you have Splunk Enterprise running locally (with an IP address of `127.0.0.1`), 
with a TCP input configured on port `15000`. TCP inputs do not have the same port number as the 
Splunk Enterprise management port.

```xml
<Appenders>
   <Socket name="SocketAppender" host="127.0.0.1" port="15000">
      <PatternLayout pattern="%p: %m%n" charset="UTF-8"/>
   </Socket>
...
   <SplunkAppender name="SplunkAppender">
      <AppenderRef ref="SocketAppender" />
   </SplunkAppender>
</Appenders>
...
<Loggers>
   <AsyncLogger name="org.apereo" level="debug">
      <AppenderRef ref="SplunkAppender"/>
   </AsyncLogger>
</Loggers>
```

Of course, you will need to create a TCP input in Splunk Enterprise to which CAS will write logs.
