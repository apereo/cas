---
layout: default
title: CAS - Papertrail Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Papertrail Logging

[Papertrail](https://papertrailapp.com) is a cloud-based log management service that provides aggregated logging tools, 
flexible system groups, team-wide access, long-term archives, charts and analytics exports, monitoring webhooks and more.

```xml
...
<Appenders>
    <Syslog name="Papertrail"
            host="<host>.papertrailapp.com"
            port="XXXXX"
            protocol="TCP" appName="MyApp" mdcId="mdc"
            facility="LOCAL0" enterpriseNumber="18060" newLine="true"
            format="RFC5424" ignoreExceptions="false" exceptionPattern="%throwable{full}">
    </Syslog>
</Appenders>
...
<Loggers>
    <Root level="INFO">
        <AppenderRef ref="Papertrail" />
    </Root>
</Loggers>
```
