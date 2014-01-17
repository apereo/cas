---
layout: default
title: CAS - Logging Configuration
---

#Logging 
CAS provides a logging facility that logs important informational events like authentication success and failure; it can be customized to produce additional information for troubleshooting. CAS uses the Slf4J Logging framework as a facade for the [Log4J engine](logging.apache.org/log4j/‎) by default. 

The log4j configuration file is located in `cas-server-webapp/src/main/webapp/WEB-INF/classes/log4j.xml`. By default logging is set to `INFO` for all functionality related to `org.jasig.cas` code and `WARN` for messages related to Spring framework, etc. For debugging and diagnostic purposes you may want to set these levels to  `DEBUG`. 

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>When in production though, you probably want to run them both as `WARN`.</p></div>

##Components
The log4j configuration is by default loaded using the following components at `cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/log4jConfiguration.xml`:

{% highlight xml %}
<bean id="log4jInitialization" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"
    p:targetClass="org.springframework.util.Log4jConfigurer" p:targetMethod="initLogging" p:arguments-ref="arguments"/>

<util:list id="arguments">
   <value>${log4j.config.location:classpath:log4j.xml}</value>
   <value>${log4j.refresh.interval:60000}</value>
</util:list>
{% endhighlight %}

It is often time helpful to externalize `log4j.xml` to a system path to preserve settings between upgrades. The location of `log4j.xml` file as well as its refresh interval by default is on the runtime classpath and at minute intervals respective. These may be overriden by the `cas.properties` file
{% highlight bash %}
# log4j.config.location=classpath:log4j.xml
#
# log4j refresh interval in millis
# log4j.refresh.interval=60000
{% endhighlight %}

##Configuration
The `log4j.xml` file by default at `WEB-INF/classes` provides the following `appender` elements that decide where and how messages from components should be displayed. Two are provided by default that output messages to the system console and a `cas.log` file:

###Appenders
{% highlight xml %}
<appender name="console" class="org.apache.log4j.ConsoleAppender">
    <layout class="org.apache.log4j.PatternLayout">
        <param name="ConversionPattern" value="%d %p [%c] - &lt;%m&gt;%n"/>
    </layout>
</appender>

<appender name="cas" class="org.apache.log4j.RollingFileAppender">
    <param name="File" value="cas.log" />
    <param name="MaxFileSize" value="512KB" />
    <param name="MaxBackupIndex" value="3" />
    <layout class="org.apache.log4j.PatternLayout">
        <param name="ConversionPattern" value="%d %p [%c] - %m%n"/>
    </layout>
</appender>
{% endhighlight %}

###Loggers
Additional loggers are available to specify the logging level for component categories.

{% highlight xml %}
<logger name="org.springframework">
    <level value="WARN" />
</logger>

<logger name="org.springframework.webflow">
    <level value="WARN" />
</logger>

<logger name="org.jasig" additivity="true">
    <level value="INFO" />
    <appender-ref ref="cas" />
</logger>

<logger name="com.github.inspektr.audit.support.Slf4jLoggingAuditTrailManager">
    <level value="INFO" />
    <appender-ref ref="cas" />
</logger>

<logger name="org.jasig.cas.web.flow" additivity="true">
    <level value="INFO" />
    <appender-ref ref="cas" />
</logger>
{% endhighlight %}

##Performance Statistics
CAS also uses the [Perf4J framework](http://perf4j.codehaus.org/), that provides set of utilities for calculating and displaying performance statistics. Similar to above, there are specific appenders and loggers available for logging performance data.

###Appenders
{% highlight xml %}
<appender name="CoalescingStatistics" class="org.perf4j.log4j.AsyncCoalescingStatisticsAppender">
    <param name="TimeSlice" value="60000"/>
    <appender-ref ref="fileAppender"/>
    <appender-ref ref="graphExecutionTimes"/>
    <appender-ref ref="graphExecutionTPS"/>
</appender>

<!-- This file appender is used to output aggregated performance statistics -->
<appender name="fileAppender" class="org.apache.log4j.FileAppender">
    <param name="File" value="perfStats.log"/>
    <layout class="org.apache.log4j.PatternLayout">
        <param name="ConversionPattern" value="%m%n"/>
    </layout>
</appender>

<appender name="graphExecutionTimes" class="org.perf4j.log4j.GraphingStatisticsAppender">
    <!-- Possible GraphTypes are Mean, Min, Max, StdDev, Count and TPS -->
    <param name="GraphType" value="Mean"/>
    <!-- The tags of the timed execution blocks to graph are specified here -->
    <param name="TagNamesToGraph" value="DESTROY_TICKET_GRANTING_TICKET,GRANT_SERVICE_TICKET,GRANT_PROXY_GRANTING_TICKET,VALIDATE_SERVICE_TICKET,CREATE_TICKET_GRANTING_TICKET,AUTHENTICATE" />
</appender>

<appender name="graphExecutionTPS" class="org.perf4j.log4j.GraphingStatisticsAppender">
    <param name="GraphType" value="TPS" />
    <param name="TagNamesToGraph" value="DESTROY_TICKET_GRANTING_TICKET,GRANT_SERVICE_TICKET,GRANT_PROXY_GRANTING_TICKET,VALIDATE_SERVICE_TICKET,CREATE_TICKET_GRANTING_TICKET,AUTHENTICATE" />
</appender>
{% endhighlight %}

###Loggers
{% highlight xml %}
<logger name="org.perf4j.TimingLogger" additivity="false">
    <level value="INFO" />
    <appender-ref ref="CoalescingStatistics" />
</logger>
{% endhighlight %}