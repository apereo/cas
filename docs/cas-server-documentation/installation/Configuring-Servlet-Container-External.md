---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# External Servlet Container Configuration

A CAS deployment may be deployed to any number of external servlet containers. The container **MUST** support
the servlet specification `5.0.0` at a minimum. In these scenarios, the following vanilla CAS web application
may be used, in the [WAR Overlay](WAR-Overlay-Installation.html) :

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp" %}

While there is no official project support, the following containers should be compatible with a CAS deployment:

* [Apache Tomcat](https://tomcat.apache.org/) (At a minimum, Apache Tomcat 10 is required)
* [JBoss](https://www.jboss.org/)
* [Wildfly](https://wildfly.org/)
* [Undertow](http://undertow.io/)
* [Jetty](https://www.eclipse.org/jetty/) (At a minimum, Jetty 11 is required)
* [GlassFish](https://glassfish.java.net/)
* [WebSphere](https://www.ibm.com/cloud/websphere-hybrid-edition)

Remember that an external container's configuration is **NEVER** automated by CAS in any way which means you are 
responsible for upgrades, maintenance and all other manners of configuration such as logging, SSL, etc. CAS does 
not provide official support and troubleshooting guidelines, etc for an external container's configuration or issues. 
Refer to the servlet container's own documentation for more info.

Note for JBoss, Wildfly and EAP, you may need to add a `jboss-deloyment-structure.xml` file to `src/main/webapp/WEB-INF` in your overlay in order for CAS to start properly.

```xml
<jboss-deployment-structure>
    <deployment>
        <dependencies>
            <module name="jdk.unsupported" />
        </dependencies>
    </deployment>
</jboss-deployment-structure>
```

## Configuration

Support for external containers is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-webapp" %}

## Async Support

In the event that an external servlet container is used, you MAY need to make sure it's configured correctly to 
support asynchronous requests in the event you get related errors and your container requires this. This is 
typically handled by setting `<async-supported>true</async-supported>` inside the container's main `web.xml`  
file (i.e. For Apache Tomcat, that would be `$CATALINA_HOME/conf/web.xml`).

## Logging

When using an external container, you may need to ensure that logging configuration file that 
ships with CAS by default is disabled and turned into a no-op **specially** if the log 
configuration and location is to be controlled via CAS settings. This is required 
because initialization of the CAS web applications context 
inside an external servlet container tends to prematurely initialize the log configuration 
from classpath before CAS itself has had a chance to control logging via settings.

To disable CAS' own logging, define a `log4j2.xml` under `src/main/resources` and put the following content in it:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Configuration>
    <Appenders>
        <Console name="console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d %p [%c] - &lt;%m&gt;%n" />
        </Console>
    </Appenders>
    <Loggers>
        <AsyncRoot level="off">
            <AppenderRef ref="console"/>
        </AsyncRoot>
    </Loggers>
</Configuration>
```

The above configuration will turn the logging initialization moot, allowing 
the location and configuration of logs to be defined via CAS settings.

### Async Logging

CAS logging automatically inserts itself into the runtime application context and will clean up
the logging context once the container is instructed to shut down. However, Apache Tomcat in particular 
seems to by default ignore all JAR files named `log4j*.jar`, which prevents this feature from working. 
You may need to change the `catalina.properties` and remove `log4j*.jar` from the `jarsToSkip` property. Failure 
to do so will prevent the container to gracefully shut down and causes logger context threads to hang.

You may need to do something similar on other containers if they skip scanning Log4j JAR files.
