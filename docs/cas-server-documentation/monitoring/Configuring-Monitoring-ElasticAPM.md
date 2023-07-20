---
layout: default
title: CAS - Elastic APM Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# Elastic Application Performance Monitoring (APM)

[Elastic Application Performance Monitoring (APM)](https://www.elastic.co/observability/application-performance-monitoring) provides the ability
to start streaming, viewing, and analyzing APM traces from CAS using Elastic Cloud.

Support is added by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-elastic" %}

The CAS integration with Elastic APM allows one to:

- Automatically launch CAS with the Elastic APM agent attached on startup for monitoring and observability
- Monitoring of CAS components and their execution trace that is then reported to Elastic APM as *Exit Span*s.
               
## Elastic APM Agent

By default, CAS would attempt to auto-attach the Elastic APM Agent to the running JVM process. If you wish to control and/or
disable this behavior, you may specify a `ELASTIC_APM_AGENT_ENABLED=false` setting as either a system property or environment variable.

Remember that there can only be one agent instance with one configuration per JVM. 
So if you deploy multiple web applications (in addition to CAS) to the same server (i.e. Apache Tomcat) 
where the agent is programmatically attached in each application, the first attach operation wins and the second one will be ignored. 
That also means that if you are configuring the agent with `elasticapm.properties`, the application which attaches first gets to decide the configuration. 

The agent configuration typically is controlled via a `src/main/resources/elasticapm.properties` properties file:    
             
```properties
service_name=apereo-cas
application_packages=org.apereo
server_url=http://127.0.0.1:8800
```
   
Consult the [Elastic APM documentation](https://www.elastic.co/guide/en/apm/agent/java/current/configuration.html) 
for additional configuration options.
  
## Spans

Component execution is traced via what Elastic APM calls *Exit Span*s, which are automatically 
created and ended as a child of the current active transaction. The created span will be used to create a node in the *Service Map* 
and a downstream service in the *Dependencies Table*. The span name, subtype (i.e. Component) and action (i.e. Operation) 
are used to group similar spans together.

<img width="1016" alt="image" src="https://user-images.githubusercontent.com/1205228/234508491-525aee39-440c-4ba1-8e25-19723027c032.png">
