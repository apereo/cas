---
layout: default
title: CAS - Logging Configuration
---

# Logging

CAS provides a logging facility that logs important informational events like authentication success and
failure; it can be customized to produce additional information for troubleshooting. CAS uses the Slf4J
Logging framework as a facade for the [Log4J engine](http://logging.apache.org) by default.

The default log4j configuration file is located in `src/main/resources/log4j2.xml`.
By default logging is set to `INFO` for all functionality related to `org.apereo.cas` code. For debugging and diagnostic purposes you may want to set these levels to  `DEBUG`.

<div class="alert alert-warning"><strong>Production</strong><p>You should always run everything under
<code>WARN</code>. In production
warnings and errors are things you care about. Everything else is just diagnostics. Only
turn up <code>DEBUG</code> or <code>INFO</code> if you need to research a particular issue.</p></div>

## Configuration

It is often time helpful to externalize the `log4j2.xml` file to a system path to preserve settings between upgrades.
The location of `log4j2.xml` file by default is on the runtime classpath and can be controlled
via the CAS properties. To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#logging).

<div class="alert alert-info"><strong>Monitoring Logs</strong><p>To review log settings and output,
 you may also use the <a href="Monitoring-Statistics.html">CAS administration panels.</a></p></div>

### Log Levels

While log levels can directly be massaged via the native `log4j2.xml` syntax, they may also be modified
using the usual CAS properties. To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#logging).

### Refresh Interval

The `log4j2.xml` itself controls the refresh interval of the logging configuration. Log4j has the ability
to automatically detect changes to the configuration file and reconfigure itself. If the `monitorInterval`
attribute is specified on the configuration element and is set to a non-zero value then the file will be
checked the next time a log event is evaluated and/or logged and the `monitorInterval` has elapsed since
the last check. This will allow you to adjust the log levels and configuration without restarting the
server environment.

```xml
<!-- Specify the refresh internal in seconds. -->
<Configuration monitorInterval="15" ...>
        ...
</Configuration>
```

## Log Data Sanitation

For security purposes, CAS by default will attempt to remove TGT and PGT ids from all log data.
This will of course include messages that are routed to a log destination by the logging framework as
 well as all audit messages. A sample follows below:

```bash
WHO: audit:unknown
WHAT: TGT-****************************************************123456-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_DESTROYED
APPLICATION: CAS
WHEN: Sat Jul 12 04:10:35 PDT 2014
CLIENT IP ADDRESS: ...
SERVER IP ADDRESS: ...
```

Certain number of characters are left at the trailing end of the ticket id to assist with
troubleshooting and diagnostics.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#logging).

## Apache Tomcat AsyncLoggers Shutdown

Log4j automatically inserts itself into the runtime application context (i.e. Tomcat) and will clean up
the logging context once the container is instructed to shut down. However,
Apache Tomcat seem to by default ignore all JAR files named `log4j*.jar`, which prevents
this feature from working. You may need to change the `catalina.properties`
and remove `log4j*.jar` from the `jarsToSkip` property. Failure to do so will stop Tomcat to gracefully shut down and causes logger context threads to hang.

You may need to do something similar on other containers if they skip scanning Log4j JAR files.

## Routing Logs to Sentry

Log data can be automatically routed to and integrated with [Sentry](../integration/Sentry-Integration.html) to track and monitor CAS events and errors.

## Routing Logs to Logstash

CAS logging framework has the ability route log messages to a TCP/UDP endpoint.
This configuration assumes that the Logstash server has enabled its [TCP input](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-tcp.html) on port `9500`:

```xml
...
<Appenders>
    <Socket name="socket" host="localhost" connectTimeoutMillis="3000"
            port="9500" protocol="TCP" ignoreExceptions="false">
      <SerializedLayout />
    </Socket>
</Appenders>
...
<AsyncLogger name="org.apereo" additivity="true" level="debug">
    <appender-ref ref="cas" />
    <appender-ref ref="socket" />
</AsyncLogger>

```

## Routing Logs to SysLog

CAS logging framework does have the ability to route messages to an external
syslog instance. To configure this,
you first to configure the `SysLogAppender` and then specify which
messages needs to be routed over to this instance:

```xml
...
<Appenders>
    <Syslog name="SYSLOG" format="RFC5424" host="localhost" port="8514"
            protocol="TCP" appName="MyApp" includeMDC="true"
            facility="LOCAL0" enterpriseNumber="18060" newLine="true"
            messageId="Audit" id="App"/>
</Appenders>
...
<AsyncLogger name="org.apereo" additivity="true" level="debug">
    <appender-ref ref="cas" />
    <appender-ref ref="SYSLOG" />
</AsyncLogger>

```

You can also configure the remote destination output over
SSL and specify the related keystore configuration:

```xml
...

<Appenders>
    <TLSSyslog name="bsd" host="localhost" port="6514">
      <SSL>
        <KeyStore location="log4j2-keystore.jks" password="changeme"/>
        <TrustStore location="truststore.jks" password="changeme"/>
      </SSL>
    </TLSSyslog>
</Appenders>

...

```

## Mapped Diagnostic Context

To uniquely stamp each request, CAS puts contextual
information into the `MDC`, the abbreviation of Mapped Diagnostic Context. This effectively
translates to a number of special variables available to the logging context that
may convey additional information about the nature of the request or the authentication event.

| Variable                                     | Description
|-----------------------------------|-------------------------------------
| `remoteAddress`                     | Remote address of the HTTP request.
| `remoteUser`                        | Remote user of the HTTP request.
| `serverName`                        | Server name of the HTTP request.
| `serverPort`                        | Server port of the HTTP request.
| `locale`                            | Locale of the HTTP request.
| `contentType`                       | Content type of the HTTP request.
| `contextPath`                       | Context path of the HTTP request.
| `localAddress`                      | Local address of the HTTP request.
| `localPort`                         | Local port of the HTTP request.
| `remotePort`                        | Remote port of the HTTP request.
| `pathInfo`                          | Path information of the HTTP request.
| `protocol`                          | Protocol of the HTTP request.
| `authType`                          | Authentication type of the HTTP request.
| `method`                            | Method of the HTTP request.
| `queryString`                       | Query string of the HTTP request.
| `requestUri`                        | Request URI of the HTTP request.
| `scheme`                            | Scheme of the HTTP request.
| `timezone`                          | Timezone of the HTTP request.
| `principal`                         | CAS authenticated principal id.

Additionally, all available request attributes and parameters are exposed as variables.

The above variables may be used in logging patterns:

- Use `%X` by itself to include all variables.
- Use `%X{key}` to include the specified variable.

```xml
<Console name="console" target="SYSTEM_OUT">
    <PatternLayout pattern="%X{locale} %d %p [%c] - &lt;%m&gt;%n"/>
</Console>
```
