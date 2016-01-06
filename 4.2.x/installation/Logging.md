---
layout: default
title: CAS - Logging Configuration
---


#Logging
CAS provides a logging facility that logs important informational events like authentication success and
failure; it can be customized to produce additional information for troubleshooting. CAS uses the Slf4J
Logging framework as a facade for the [Log4J engine](http://logging.apache.org‎) by default.

The log4j configuration file is located in `cas-server-webapp/src/main/webapp/WEB-INF/classes/log4j2.xml`.
By default logging is set to `INFO` for all functionality related to `org.jasig.cas` code and `WARN` for
messages related to Spring framework, etc. For debugging and diagnostic purposes you may want to set
these levels to  `DEBUG`.

{% highlight xml %}
...

<Logger name="org.jasig" level="info" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>

<Logger name="org.springframework" level="warn" />
...
{% endhighlight %}

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>When in production though,
you probably want to run them both as `WARN`.</p></div>


##Components
The log4j configuration is by default loaded using the following components
at `cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/log4jConfiguration.xml`:

{% highlight xml %}
<bean id="log4jInitialization" class="org.jasig.cas.util.CasLoggerContextInitializer"
    c:logConfigurationField="log4jConfiguration"
    c:logConfigurationFile="${log4j.config.location:classpath:log4j2.xml}"
    c:loggerContextPackageName="org.apache.logging.log4j.web"/>
{% endhighlight %}

It is often time helpful to externalize `log4j2.xml` to a system path to preserve settings between upgrades.
The location of `log4j2.xml` file by default is on the runtime classpath and at minute intervals
respective. These may be overridden by the `cas.properties` file

{% highlight bash %}
# log4j.config.location=classpath:log4j2.xml
{% endhighlight %}


##Configuration
The `log4j2.xml` file by default at `WEB-INF/classes` provides the following `appender` elements that
decide where and how messages from components should be displayed. Two are provided by default that
output messages to the system console and a `cas.log` file:

###Refresh Interval
The `log4j2.xml` itself controls the refresh interval of the logging configuration. Log4j has the ability
to automatically detect changes to the configuration file and reconfigure itself. If the `monitorInterval`
attribute is specified on the configuration element and is set to a non-zero value then the file will be
checked the next time a log event is evaluated and/or logged and the `monitorInterval` has elapsed since
the last check. This will allow you to adjust the log levels and configuration without restarting the
server environment.

{% highlight xml %}
<!-- Specify the refresh internal in seconds. -->
<Configuration monitorInterval="60">
    <Appenders>
        ...
{% endhighlight %}

###Appenders
{% highlight xml %}
<Console name="console" target="SYSTEM_OUT">
    <PatternLayout pattern="%d %p [%c] - &lt;%m&gt;%n"/>
</Console>
<RollingFile name="file" fileName="cas.log" append="true"
             filePattern="cas-%d{yyyy-MM-dd-HH}-%i.log">
    <PatternLayout pattern="%d %p [%c] - %m%n"/>
    <Policies>
        <OnStartupTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="10 MB"/>
        <TimeBasedTriggeringPolicy />
    </Policies>
</RollingFile>
{% endhighlight %}


###Loggers
Additional loggers are available to specify the logging level for component categories.

{% highlight xml %}
<Logger name="org.jasig" level="info" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
<Logger name="org.springframework" level="warn" />
<Logger name="org.springframework.webflow" level="warn" />
<Logger name="org.springframework.web" level="warn" />
<Logger name="org.springframework.security" level="warn" />

<Logger name="org.jasig.cas.web.flow" level="info" additivity="true">
    <AppenderRef ref="file"/>
</Logger>
<Logger name="org.jasig.inspektr.audit.support" level="info">
    <AppenderRef ref="file"/>
</Logger>
<Root level="error">
    <AppenderRef ref="console"/>
</Root>
{% endhighlight %}

If you wish enable another package for logging, you can simply add another `Logger`
element to the configuration. Here is an example:

{% highlight xml %}
<Logger name="org.ldaptive" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
{% endhighlight %}

##Log Data Sanitation
For security purposes, CAS by default will attempt to remove TGT and PGT ids from all log data.
This will of course include messages that are routed to a log destination by the logging framework as
 well as all audit messages. A sample follows below:

{% highlight bash %}
=============================================================
WHO: audit:unknown
WHAT: TGT-****************************************************123456-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_DESTROYED
APPLICATION: CAS
WHEN: Sat Jul 12 04:10:35 PDT 2014
CLIENT IP ADDRESS: ...
SERVER IP ADDRESS: ...
=============================================================
{% endhighlight %}

Certain number of characters are left at the trailing end of the ticket id to assist with
troubleshooting and diagnostics. This is achieved by providing a specific binding for the SLF4j configuration.
