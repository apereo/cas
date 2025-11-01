---
layout: default
title: CAS - Logging Configuration
category: Logs & Audits
---

# Logging

CAS provides a logging facility that logs important informational events like authentication success and
failure; it can be customized to produce additional information for troubleshooting. CAS uses the Slf4j
Logging framework as a facade for the [Log4j engine](http://logging.apache.org) by default.

The default log4j configuration file is located in `src/main/resources/log4j2.xml` of the `cas-server-webapp-resources` 
source module. In the `cas.war` it is found at the root of the `cas-server-webapp-resources*.jar`. 
The cas-overlay comes with an external log42.xml in etc/cas/config and a property 
`logging.config=file:/etc/cas/config/log4j2.xml` set to reference it. 
By default logging is set to `INFO` for all functionality related to `org.apereo.cas` code.
For debugging and diagnostic purposes you may want to set these levels to `DEBUG` or `TRACE`.

<div class="alert alert-warning"><strong>Production</strong><p>You should always run everything under
<code>WARN</code>. In production warnings and errors are things you care about. Everything else is just diagnostics. Only
turn up <code>DEBUG</code> or <code>INFO</code> if you need to research a particular issue.</p></div>

## CAS Custom Log4j2 plugins
The log4j2.xml file use by CAS includes custom Log4j2 plugins:
- CasAppender: The CasAppender wraps another regular appender and removes sensitive values from the log entries
such as Ticket Granting Tickets or Proxy Granting Tickets.
- ExceptionOnlyFilter: In order to allow CAS to freely log unexpected errors at WARN and ERROR without obscuring everything
with stacktraces, exceptions in the logs are disabled by default but there are log4j2.xml properties that can turn 
them back on. By default, all exceptions are written to a dedicated stacktrace rolling log file 
and this is done using a custom ExceptionOnlyFilter nested in the CasAppender. 

## Log4j2 Properties
The log4j2.xml file includes properties for various settings and those can be set in the properties section
of the log4j2.xml file, in a property file called `log4j2.component.properties` on the classpath, or as system 
properties. If setting properties in a `log4j2.component.properties`, be sure to include:
```
Log4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```
in order to keep using asynchronous logging which CAS sets by default. 
To turn off asynchronous logging, include the following in `log4j2.component.properites` or as a system property:
```
Log4jContextSelector=org.apache.logging.log4j.core.selector.BasicContextSelector
```

## Configuration

It is often helpful to externalize the `log4j2.xml` file to a system path to preserve settings between upgrades.
The location of `log4j2.xml` file by default is on the runtime classpath and can be controlled
via the CAS properties. 

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#logging).

### Log Levels

While log levels can directly be massaged via the native `log4j2.xml` syntax, they may also be modified
using the usual CAS properties. To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#logging).

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

### Appenders

Appenders are responsible for delivering log events to their destination. Appenders usually are only responsible for writing the event data to the 
target destination. In most cases they delegate responsibility for formatting the event to a layout. Some appenders wrap other appenders so that they can modify the log event, 
handle a failure in an `Appender`, route the event to a subordinate `Appender` based on advanced filtering criteria or provide similar 
functionality that does not directly format the event for viewing. `Appender`s always have a name so that they can be referenced from `Logger`s.

The following `Appender` elements are only a partial collection of available options. 

| Layout                   | Description
|--------------------------|------------------------------------------------------------------------
| `AsyncAppender`          | Accepts references to other Appenders and causes LogEvents to be written to them on a separate Thread.
| `CassandraAppender`      | Writes its output to an Apache Cassandra database. A keyspace and table must be configured ahead of time, and the columns should be mapped in a configuration file. 
| `ConsoleAppender`        | Writes its output to either `System.out` or `System.err` with `System.out` being the default target.
| `FailoverAppender`       | Wraps a set of appenders. If the primary Appender fails the secondary appenders will be tried in order until one succeeds or there are no more secondaries to try.
| `FileAppender`           | Writes to the File named in the `fileName` parameter.
| `CsvParameterLayout`     | Converts an event's parameters into a CSV record, ignoring the message.
| `JDBCAppender`           | Writes log events to a relational database table using standard JDBC.
| `JPAAppender`            | Writes log events to a relational database table using the Java Persistence API `2.1`.
| `HttpAppender`           | Sends log events over HTTP. A Layout must be provided to format the log event.
| `KafkaAppender`          | Logs events to an Apache Kafka topic. Each log event is sent as a Kafka record.
| `NoSQLAppender`          | Writes log events to a NoSQL database; Provider implementations currently exist for MongoDB and Apache CouchDB.
| `RoutingAppender`        | Evaluates log events and then routes them to a subordinate `Appender`. 
| `SMTPAppender`           | Sends an e-mail when a specific logging event occurs, typically on errors or fatal errors.
| `JeroMQ`                 | The ZeroMQ appender uses the JeroMQ library to send log events to one or more ZeroMQ endpoints.
| `RollingFileAppender`    | Writes to the File named in the fileName parameter and rolls the file over according the `TriggeringPolicy` and the `RolloverPolicy`.
| `RewriteAppender`        | Allows the log event to be manipulated before it is processed by another `Appender`. This can be used to mask sensitive information such as passwords or to inject information into each event. 

For full details, please review the official [Log4j documentation](http://logging.apache.org)

### Log Patterns

By default most appenders that are provided via the `log4j2.xml` file use
pattern-based layouts to format log messages. The following alternative layouts may also be used:

| Layout                        | Description
|-------------------------------|------------------------------------------------------------------------
| `CsvParameterLayout`          | Converts an event's parameters into a CSV record, ignoring the message.
| `GelfLayout`                  | Lays out events in the Graylog Extended Log Format (`GELF`).
| `HTMLLayout`                  | Generates an HTML page and adds each LogEvent to a row in a table
| `JSONLayout`                  | Creates log events in well-formed or fragmented JSON.
| `PatternLayout`               | Formats the log even based on a conversion pattern.
| `RFC5424Layout`               | Formats log events in accordance with [RFC 5424](https://tools.ietf.org/html/rfc5424), the enhanced Syslog specification.
| `SerializedLayout`            | Log events are transformed into byte arrays useful in JMS or socket connections.
| `SyslogLayout`                | Formats log events as BSD Syslog records.
| `XMLLayout`                   | Creates log events in well-formed or fragmented XML.
| `YamlLayout`                  | Creates log events in YAML.

To learn more about nuances and configuration settings for each, please refer to the [official Log4J guides](http://logging.apache.org).

## Log File Rotation

The default configuration specifies triggering policies for rolling over logs, at startup, size or at specific times. These policies apply to `RollingFile` appenders.

For example, the following XML fragment defines policies that rollover the log when the JVM starts, when the log size reaches `10` megabytes, and when the current date no longer matches the log’s start date.

```xml
<RollingFile name="file" fileName="${baseDir}/cas.log" append="true"
                    filePattern="${baseDir}/cas-%d{yyyy-MM-dd-HH}-%i.log">
    ...
    <Policies>
        <OnStartupTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="10 MB"/>
        <TimeBasedTriggeringPolicy interval="24" />
    </Policies>
    ...
</RollingFile>
```

The triggering policies determines **if** a rollover should be performed and rollover strategy can also be design to indicate **how** that should be done. If no strategy is configured, the default will be used.

To find more a comprehensive documentation, please [review the guides here](http://logging.apache.org).

### Rollover Strategy

Customized rollover strategies provide a delete action that gives users more control over what files are deleted at rollover time than what was possible with the DefaultRolloverStrategy max attribute. The delete action lets users configure one or more conditions that select the files to delete relative to a base directory.

For example, the following appender at rollover time deletes all files under the base directory that match the `*/*.log` glob and are `7` days old or older.

```xml
<RollingFile name="file" fileName="${baseDir}/cas.log" append="true"
             filePattern="${baseDir}/cas-%d{yyyy-MM-dd-HH}-%i.log">
    ...
    <DefaultRolloverStrategy max="5">
        <Delete basePath="${baseDir}" maxDepth="2">
            <IfFileName glob="*/*.log" />
            <IfLastModified age="7d" />
        </Delete>
    </DefaultRolloverStrategy>
    ...
</RollingFile>
```

To find more a comprehensive documentation, please [review the guides here](http://logging.apache.org).

## Log Data Sanitation

For security purposes, CAS by default will attempt to remove ticket-granting ticket and proxy-granting ticket ids from all log data.
This will of course include messages that are routed to a log destination by the logging framework as well as all audit messages.

A sample follows below:

```bash
WHO: audit:unknown
WHAT: TGT-******************123456-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_DESTROYED
APPLICATION: CAS
WHEN: Sat Jul 12 04:10:35 PDT 2014
CLIENT IP ADDRESS: ...
SERVER IP ADDRESS: ...
```

Certain number of characters are left at the trailing end of the ticket id to assist with troubleshooting and diagnostics.

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#logging).
