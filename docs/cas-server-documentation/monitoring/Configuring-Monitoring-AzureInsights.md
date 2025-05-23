---
layout: default
title: CAS - Azure Monitor Application Insights
category: Monitoring & Statistics
---

{% include variables.html %}

# Azure Monitor Application Insights

[Azure Monitor Application Insights](https://learn.microsoft.com/en-us/azure/azure-monitor/overview) is 
a feature of Azure Monitor that offers application performance 
monitoring (APM) for CAS as a web applications. 
Support is added by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-azure-monitor" %}

The CAS integration with Azure Monitor allows one to automatically launch CAS with 
the Azure Monitor Application Insights agent attached on startup for monitoring and observability.
               
## Azure Application Insights Agent

By default, CAS would attempt to auto-attach the agent to the running JVM process. If you wish to control and/or
disable this behavior, you may specify a `AZURE_MONITOR_AGENT_ENABLED=false` setting as either a system property or environment variable.

The following considerations apply to the agent:

- JRE distributions are not supported.
- The temporary directory of the operating system should be writable.
- Spring’s `application.properties` or `application.yaml` files are not supported as sources for Application Insights configuration.

By default the configuration file `applicationinsights.json` is read from the 
classpath from `src/main/resources`. You can configure the name of the JSON file in the classpath with the
`applicationinsights.runtime-attach.configuration.classpath.file` system property. You 
may also use the system property `applicationinsights.configuration.file` to configure a file outside the classpath.

By default, the `applicationinsights.log` file containing the agent logs are located in the directory from where the JVM is launched.
