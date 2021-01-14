---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Configuration

Note that by default, the embedded container attempts to enable the HTTP2 protocol.

{% include casmodule.html group="org.apereo.cas" module="cas-server-webapp-tomcat" %}

## IPv4 Configuration

In order to force Apache Tomcat to use IPv4, configure the following as a system property for your *run* command:

```bash
-Djava.net.preferIPv4Stack=true 
```

The same sort of configuration needs to be applied to your `$CATALINA_OPTS` 
environment variable in case of an external container.

## Faster Startup

[This guide](https://cwiki.apache.org/confluence/display/TOMCAT/HowTo+FasterStartUp) provides 
several recommendations on how to make web applications and Apache Tomcat as a whole to start up faster.

## Logging

The embedded Apache Tomcat container is presently unable to display any log messages below `INFO` even if your CAS log 
configuration explicitly asks for `DEBUG` or `TRACE` level data. 
See [this bug report](https://github.com/spring-projects/spring-boot/issues/2923) to learn more.

While workarounds and fixes may become available in the future, for the time being, you may execute the following 
changes to get `DEBUG` level log data from the embedded Apache Tomcat. This 
is specially useful if you are troubleshooting the behavior 
of Tomcat's internal components such as valves, etc.

- Design a `logging.properties` file as such:

```properties
handlers = java.util.logging.ConsoleHandler
.level = ALL
java.util.logging.ConsoleHandler.level = FINER
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
```

- Design a`java.util.logging.config.file` setting as a system/environment variable or command-line 
argument whose value is set to the `logging.properties` path. Use the setting when you launch and deploy CAS.

For instance:

```bash
java -jar /path/to/cas.war -Djava.util.logging.config.file=/path/to/logging.properties
```

### Configuration

{% include casproperties.html properties="server.tomcat" %}

#### HTTP Proxying

{% include casproperties.html properties="cas.server.tomcat.http-proxy" %}

#### HTTP

{% include casproperties.html properties="cas.server.tomcat.http" %}

#### AJP

{% include casproperties.html properties="cas.server.tomcat.ajp" %}

#### SSL Valve

{% include {{ version }}/embedded-container-tomcat-sslvalve-configuration.md %}

#### Extended Access Log Valve

{% include casproperties.html properties="cas.server.tomcat.ext-access-log" %}

#### Rewrite Valve

{% include casproperties.html properties="cas.server.tomcat.rewrite-valve" %}

#### Basic Authentication

{% include casproperties.html properties="cas.server.tomcat.basic-authn" %}

#### Apache Portable Runtime (APR)

{% include {{ version }}/embedded-container-tomcat-apr-configuration.md %}

#### Connector IO

{% include casproperties.html properties="cas.server.tomcat.socket" %}

#### Session Clustering & Replication

{% include {{ version }}/embedded-container-tomcat-session-clustering-configuration.md %}
