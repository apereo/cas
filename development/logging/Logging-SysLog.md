# SysLog Logging

Source: https://apereo.github.io/cas/development/logging/Logging-SysLog.html

Send CAS log messages to a syslog server with the Log4j syslog appender.

CAS logging framework does have the ability to route messages to an external syslog instance. To configure this, you first configure the `SysLogAppender` and then specify which messages need to be routed over to this instance:

```xml
...
<Appenders>
    <Syslog name="SYSLOG" format="RFC5424" host="localhost" port="8514"
            protocol="TCP" appName="MyApp" includeMDC="true" mdcId="mdc"
            facility="LOCAL0" enterpriseNumber="18060" newLine="true"
            messageId="Audit" id="App"/>
</Appenders>
...
<Logger name="org.apereo" additivity="true" level="debug">
    <appender-ref ref="cas" />
    <appender-ref ref="SYSLOG" />
</Logger>
```

> **Pay Attention**
> [Mapped Diagnostic Context](https://apereo.github.io/cas/development/logging/Logging-MDC.html) (`MDC`) may contain the password. Setting `includeMDC=true` sends clear password as a variable to SysLog.

You can also configure the remote destination output over SSL and specify the related keystore configuration:

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
