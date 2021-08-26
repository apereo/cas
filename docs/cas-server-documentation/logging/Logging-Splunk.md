---
layout: default
title: CAS - Splunk Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Splunk Logging

Log data can be automatically routed to [Splunk](https://splunk.com/). Support is enabled by including the following module in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-logging-config-splunk" %}

You may also need to declare the following repository in your CAS overlay to be able to resolve dependencies:

```groovy       
repositories {
    maven { 
        mavenContent { releasesOnly() }
        url "https://splunk.jfrog.io/splunk/ext-releases-local" 
    }
}
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
   <Logger name="org.apereo" level="debug">
      <AppenderRef ref="SplunkAppender"/>
   </Logger>
</Loggers>
```

Of course, you will need to create a TCP input in Splunk Enterprise to which CAS will write logs.
