---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Embedded Servlet Container

Note that CAS itself ships with a number of embedded containers that allow the platform to be self-contained 
as much as possible. These embedded containers are an integral part of the CAS software, are maintained and 
updated usually for every release and surely are meant to and can be used in production deployments. 
You **DO NOT** need to, but can if you want to, configure and deploy to an externally configured container. 

## Configuration

{% include_cached casproperties.html 
thirdPartyStartsWith="server.port,server.ssl,server.servlet,server.max-http-header-size,server.use-forward-headers,server.connection-timeout" %}

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
 wish to see and examine the script, run the following commands:
 
 ```bash
 # X is the number of lines from the beginning of the file
 head -n X /path/to.cas.war
 ```
 
Note that running CAS as a standalone and fully executable web application 
is supported on most Linux and OS X distributions. 
Other platforms such as Windows may require custom configuration.

### Apache Tomcat

Please see [this guide](Configuring-Servlet-Container-Embedded-Tomcat.html).

### Jetty

Please see [this guide](Configuring-Servlet-Container-Embedded-Jetty.html).

### Undertow

Please see [this guide](Configuring-Servlet-Container-Embedded-Undertow.html).
