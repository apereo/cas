---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Embedded Servlet Container Configuration

Note that CAS itself ships with a number of embedded containers that allow the platform to be self-contained 
as much as possible. These embedded containers are an integral part of the CAS software, are maintained and 
updated usually for every release and surely are meant to and can be used in production deployments. 
You **DO NOT** need to, but can if you want to, configure and deploy to an externally configured container. 

## Embedded Container Configuration

{% include {{ version }}/embedded-container-configuration.md %}

## X.509 Client Authentication

{% include {{ version }}/embedded-container-x509-configuration.md %}

## Execution

The CAS web application, once built, may be deployed in place with the embedded container via the following command:

```bash
java -jar /path/to/cas.war
```

Additionally, it is also possible to run CAS as a fully executable web application:

```bash
# chmod +x /path/to/cas.war
/path/to/cas.war
```

This is achieved via the build process of the deployment overlay where a 
launch script is *inserted* at the beginning of the web application artifact. If you
 wish to see and examine the script, simply run the following commands:
 
 ```bash
 # X is the number of lines from the beginning of the file
 head -n X /path/to.cas.war
 ```
 
Note that running CAS as a standalone and fully executable web application 
is supported on most Linux and OS X distributions. 
Other platforms such as Windows may require custom configuration.

### Apache Tomcat

Note that by default, the embedded container attempts to enable the HTTP2 protocol.

{% include casmodule.html group="org.apereo.cas" module="cas-server-webapp-tomcat" %}

#### IPv4 Configuration

In order to force Apache Tomcat to use IPv4, configure the following as a system property for your *run* command:

```bash
-Djava.net.preferIPv4Stack=true 
```

The same sort of configuration needs to be applied to your `$CATALINA_OPTS` 
environment variable in case of an external container.

#### Faster Startup

[This guide](https://cwiki.apache.org/confluence/display/TOMCAT/HowTo+FasterStartUp) provides 
several recommendations on how to make web applications and Apache Tomcat as a whole to start up faster.

#### Logging

The embedded Apache Tomcat container is presently unable to display any log messages below `INFO` even if your CAS log 
configuration explicitly asks for `DEBUG` or `TRACE` level data. See [this bug report](https://github.com/spring-projects/spring-boot/issues/2923) to learn more.

While workarounds and fixes may become available in the future, for the time being, you may execute the following 
changes to get `DEBUG` level log data from the embedded Apache Tomcat. This is specially useful if you are troubleshooting the behavior 
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

### Embedded Apache Tomcat Container

{% include {{ version }}/embedded-container-tomcat-configuration.md %}

#### HTTP Proxying

{% include {{ version }}/embedded-container-tomcat-httpproxy-configuration.md %}

#### HTTP

{% include {{ version }}/embedded-container-tomcat-http-configuration.md %}

#### AJP

{% include {{ version }}/embedded-container-tomcat-ajp-configuration.md %}

#### SSL Valve

{% include {{ version }}/embedded-container-tomcat-sslvalve-configuration.md %}

#### Extended Access Log Valve

{% include {{ version }}/embedded-container-tomcat-extended-accesslog-configuration.md %}

#### Rewrite Valve

{% include {{ version }}/embedded-container-tomcat-rewritevalve-configuration.md %}

#### Basic Authentication

{% include {{ version }}/embedded-container-tomcat-basicauth-configuration.md %}

#### Apache Portable Runtime (APR)

{% include {{ version }}/embedded-container-tomcat-apr-configuration.md %}

#### Connector IO

{% include {{ version }}/embedded-container-tomcat-connectorio-configuration.md %}

#### Session Clustering & Replication

{% include {{ version }}/embedded-container-tomcat-session-clustering-configuration.md %}

### Jetty

{% include casmodule.html group="org.apereo.cas" module="cas-server-webapp-jetty" %}

### Embedded Jetty Container

{% include {{ version }}/embedded-container-jetty-configuration.md %}

### Undertow

{% include casmodule.html group="org.apereo.cas" module="cas-server-webapp-undertow" %}

{% include {{ version }}/embedded-container-undertow-configuration.md %}
