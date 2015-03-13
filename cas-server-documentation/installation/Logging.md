---
layout: default
title: CAS - Logging Configuration
---


#Logging 
CAS provides a logging facility that logs important informational events like authentication success and failure; it can be customized to produce additional information for troubleshooting. CAS uses the Slf4J Logging framework as a facade for the [Log4J engine](http://logging.apache.org/log4j/‎) by default. 

The log4j configuration file is located in `cas-server-webapp/src/main/webapp/WEB-INF/classes/log4j2.xml`. By default logging is set to `INFO` for all functionality related to `org.jasig.cas` code and `WARN` for messages related to Spring framework, etc. For debugging and diagnostic purposes you may want to set these levels to  `DEBUG`. 

{% highlight xml %}
...

<Logger name="org.jasig" level="info" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>

<Logger name="org.springframework" level="warn" />
...
{% endhighlight %}


<br/>
<div class="alert alert-warning"><strong>Usage Warning!</strong><p>When in production though, you probably want to run them both as `WARN`.</p></div>


##Components
The log4j configuration is by default loaded using the following components at `cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/log4jConfiguration.xml`:

{% highlight xml %}
<bean id="log4jInitialization" class="org.jasig.cas.util.CasLoggerContextInitializer"
    c:logConfigurationField="log4jConfiguration"
    c:logConfigurationFile="${log4j.config.location:classpath:log4j2.xml}"
    c:loggerContextPackageName="org.apache.logging.log4j.web"/>
{% endhighlight %}

It is often time helpful to externalize `log4j2.xml` to a system path to preserve settings between upgrades. The location of `log4j2.xml` file by default is on the runtime classpath and at minute intervals respective. These may be overridden by the `cas.properties` file

{% highlight bash %}
# log4j.config.location=classpath:log4j2.xml
{% endhighlight %}


##Configuration
The `log4j2.xml` file by default at `WEB-INF/classes` provides the following `appender` elements that decide where and how messages from components should be displayed. Two are provided by default that output messages to the system console and a `cas.log` file:

###Refresh Interval
The `log4j2.xml` itself controls the refresh interval of the logging configuration. Log4j has the ability to automatically detect changes to the configuration file and reconfigure itself. If the `monitorInterval` attribute is specified on the configuration element and is set to a non-zero value then the file will be checked the next time a log event is evaluated and/or logged and the `monitorInterval` has elapsed since the last check. This will allow you to adjust the log levels and configuration without restarting the server environment.

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
<Logger name="com.github.inspektr.audit.support.Slf4jLoggingAuditTrailManager" level="info">
    <AppenderRef ref="file"/>
</Logger>
<Root level="error">
    <AppenderRef ref="console"/>
        </Root>
{% endhighlight %}

If you wish enable another package for logging, you can simply add another `Logger` element to the configuration. Here is an example:

{% highlight xml %}
<Logger name="org.ldaptive" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
{% endhighlight %}

##Log Data Sanitation
For security purposes, CAS by default will attempt to remove TGT and PGT ids from all log data. This will of course include messages that are routed to a log destination by the logging framework as well as all audit messages. A sample follows below:

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

Certain number of characters are left at the trailing end of the ticket id to assist with troubleshooting and diagnostics. This is achieved by providing a specific binding for the SLF4j configuration. 

##Performance Statistics
CAS also uses the [Dropwizard Metrics framework](https://dropwizard.github.io/metrics/), that provides set of utilities for calculating and displaying performance statistics. 

###Configuration
The metrics configuration is controlled via the `/src/main/webapp/WEB-INF/spring-configuration/metricsContext.xml` file. The configuration will output all performance-related data and metrics to the logging framework. The reporting interval can be configured via the `cas.properties` file:

{% highlight bash %}

# Define how often should metric data be reported. Default is 30 seconds.
# metrics.refresh.internal=30s

{% endhighlight %}

Various metrics can also be reported via JMX. Metrics are exposes via JMX MBeans. 

{% highlight xml %}

<metrics:reporter type="jmx" metric-registry="metrics" />

{% endhighlight %}

To explore this you can use VisualVM (which ships with most JDKs as jvisualvm) with the VisualVM-MBeans plugins installed or JConsole (which ships with most JDKs as jconsole):

![](http://i.imgur.com/g8fmUlE.png)

Additionally, various metrics on JVM performance and data are also reported. The metrics contain a number of reusable gauges and metric sets which allow you to easily instrument JVM internals.

{% highlight xml %}

<metrics:register metric-registry="metrics">
    <bean metrics:name="jvm.gc" class="com.codahale.metrics.jvm.GarbageCollectorMetricSet" />
    <bean metrics:name="jvm.memory" class="com.codahale.metrics.jvm.MemoryUsageGaugeSet" />
    <bean metrics:name="jvm.thread-states" class="com.codahale.metrics.jvm.ThreadStatesGaugeSet" />
    <bean metrics:name="jvm.fd.usage" class="com.codahale.metrics.jvm.FileDescriptorRatioGauge" />
</metrics:register>

{% endhighlight %}

Supported metrics include:

- Run count and elapsed times for all supported garbage collectors
- Memory usage for all memory pools, including off-heap memory
- Breakdown of thread states, including deadlocks
- File descriptor usage
- ...
- 
###Loggers
All performance data and metrics are routed to a log file via the Log4j configuration:

{% highlight xml %}
...
<RollingFile name="perfFileAppender" fileName="perfStats.log" append="true"
             filePattern="perfStats-%d{yyyy-MM-dd-HH}-%i.log">
    <PatternLayout pattern="%m%n"/>
    <Policies>
        <OnStartupTriggeringPolicy />
        <SizeBasedTriggeringPolicy size="10 MB"/>
        <TimeBasedTriggeringPolicy />
    </Policies>
</RollingFile>

...

<Logger name="perfStatsLogger" level="info" additivity="false">
	<AppenderRef ref="perfFileAppender"/>
</Logger>

{% endhighlight %}


###Sample Output
{% highlight bash %}
type=GAUGE, name=jvm.gc.Copy.count, value=22
type=GAUGE, name=jvm.gc.Copy.time, value=466
type=GAUGE, name=jvm.gc.MarkSweepCompact.count, value=3
type=GAUGE, name=jvm.gc.MarkSweepCompact.time, value=414
type=GAUGE, name=jvm.memory.heap.committed, value=259653632
type=GAUGE, name=jvm.memory.heap.init, value=268435456
type=GAUGE, name=jvm.memory.heap.max, value=1062338560
type=GAUGE, name=jvm.memory.heap.usage, value=0.09121857348376773
type=GAUGE, name=jvm.memory.heap.used, value=96905008

type=METER, name=org.jasig.cas.CentralAuthenticationServiceImpl.CREATE_TICKET_GRANTING_TICKET_METER, count=0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=METER, name=org.jasig.cas.CentralAuthenticationServiceImpl.DESTROY_TICKET_GRANTING_TICKET_METER, count=0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond

type=TIMER, name=org.jasig.cas.CentralAuthenticationServiceImpl.GRANT_SERVICE_TICKET_TIMER, count=0, min=0.0, max=0.0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond, duration_unit=milliseconds

type=TIMER, name=org.jasig.cas.CentralAuthenticationServiceImpl.VALIDATE_SERVICE_TICKET_TIMER, count=0, min=0.0, max=0.0, mean=0.0, stddev=0.0, median=0.0, p75=0.0, p95=0.0, p98=0.0, p99=0.0, p999=0.0, mean_rate=0.0, m1=0.0, m5=0.0, m15=0.0, rate_unit=events/millisecond, duration_unit=milliseconds

{% endhighlight %}

###Viewing Metrics on the Web
The CAS web application exposes a `/statistics` endpoint that can be used to view metrics and stats in the browser. The endpoint is protected by Spring Security, and the access rules are placed inside the `cas.properties` file:

{% highlight bash %}
# Spring Security's EL-based access rules for the /statistics URI of CAS that exposes stats about the CAS server
cas.securityContext.statistics.access=hasIpAddress('127.0.0.1')
{% endhighlight %}

Once access is granted, the following sub-endpoints can be used to query the CAS server's status and metrics:

####`/statistics/ping`
Reports back `pong` to indicate that the CAS server is running.

####`/statistics/metrics?pretty=true`
Reports back metrics and performance data. The optional `pretty` flag attempts to format the JSON output.

####`/statistics/threads`
Reports back JVM thread info.

####`/statistics/healthcheck`
Unused at this point, but may be used later to output health examinations of the CAS server's internals, such as ticket registry, etc.

##Routing logs to SysLog
CAS logging framework does have the ability to route messages to an external syslog instance. To configure this, you first to configure the `SysLogAppender` and then specify which messages needs to be routed over to this instance:

{% highlight xml %}
...
<Appenders>
    <Syslog name="SYSLOG" format="RFC5424" host="localhost" port="8514"
            protocol="TCP" appName="MyApp" includeMDC="true"
            facility="LOCAL0" enterpriseNumber="18060" newLine="true"
            messageId="Audit" id="App"/>
</Appenders>

...

<logger name="org.jasig" additivity="true">
    <level value="DEBUG" />
    <appender-ref ref="cas" />
    <appender-ref ref="SYSLOG" />
</logger>

{% endhighlight %}

You can also configure the remote destination output over SSL and specify the related keystore configuration:

{% highlight xml %}
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

{% endhighlight %}

For additional logging functionality, please refer to the Log4j configuration url.  

#Audits
CAS uses the [Inspektr framework](https://github.com/dima767/inspektr) for auditing purposes and statistics. The Inspektr project allows for non-intrusive auditing and logging of the coarse-grained execution paths e.g. Spring-managed beans method executions by using annotations and Spring-managed `@Aspect`-style aspects.

##Components

###`AuditTrailManagementAspect`
Aspect modularizing management of an audit trail data concern.


###`Slf4jLoggingAuditTrailManager`
`AuditTrailManager` that dumps auditable information to a configured logger based on SLF4J, at the `INFO` level.


###`JdbcAuditTrailManager`
`AuditTrailManager` to persist the audit trail to the `AUDIT_TRAIL` table in a rational database.


###`TicketAsFirstParameterResourceResolver`
`ResourceResolver` that can determine the ticket id from the first parameter of the method call.


###`TicketOrCredentialPrincipalResolver`
`PrincipalResolver` that can retrieve the username from either the `Ticket` or from the `Credential`.

##Configuration
Audit functionality is specifically controlled by the `WEB-INF/spring-configuration/auditTrailContext.xml`. Configuration of the audit trail manager is defined inside `deployerConfigContext.xml`.


###Database Audits
By default, audit messages appear in log files via the `Slf4jLoggingAuditTrailManager`. If you intend to use a database for auditing functionality, adjust the audit manager to match the sample configuration below:
{% highlight xml %}
<bean id="auditManager" class="com.github.inspektr.audit.support.JdbcAuditTrailManager">
  <constructor-arg index="0" ref="inspektrTransactionTemplate" />
  <property name="dataSource" ref="dataSource" />
  <property name="cleanupCriteria" ref="auditCleanupCriteria" />
</bean>
<bean id="auditCleanupCriteria"
  class="com.github.inspektr.audit.support.MaxAgeWhereClauseMatchCriteria">
  <constructor-arg index="0" value="180" />
</bean>
{% endhighlight %}

Refer to [Inspektr documentation](https://github.com/dima767/inspektr/wiki/Inspektr-Auditing) on how to create the database schema.


##Sample Log Output
{% highlight bash %}
WHO: org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: supplied credentials: org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
ACTION: AUTHENTICATION_SUCCESS
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22

WHO: org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: TGT-9-qj2jZKQUmu1gQvXNf7tXQOJPOtROvOuvYAxybhZiVrdZ6pCUwW-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_CREATED
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22
{% endhighlight %}
