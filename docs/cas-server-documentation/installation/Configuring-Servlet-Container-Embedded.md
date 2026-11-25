---
layout: default
title: CAS - Embedded Servlet Container
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
thirdPartyStartsWith="server.port,server.ssl,server.servlet,server.request.header.forwarded" %}

### Behind a Proxy

When CAS runs behind a proxy or load balancer, the forwarded header settings above decide how CAS treats the headers
the proxy adds: whether the embedded container (`web-server`) or the application (`framework`) applies them, or neither
(`none`), and whether CAS reads the `X-Forwarded-*` headers (the default) or the standard `Forwarded` header. The
`X-Forwarded-Prefix` header is ignored unless it is turned on. With the `web-server` strategy, the embedded Apache Tomcat
applies neither that header nor the `Forwarded` header, so use the `framework` strategy for either; CAS fails to start
when the container cannot apply the chosen combination.

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

The following embedded servlet containers are available:

| Option        | Reference                                                                      |
|---------------|--------------------------------------------------------------------------------|
| Apache Tomcat | Please see [this guide](Configuring-Servlet-Container-Embedded-Tomcat.html).   |
| Jetty         | Please see [this guide](Configuring-Servlet-Container-Embedded-Jetty.html).    |
