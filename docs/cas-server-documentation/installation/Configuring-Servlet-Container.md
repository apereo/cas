---
layout: default
title: CAS - Overlay Installation
---

# Servlet Container Configuration

A number of container options are available to deploy CAS. The [WAR Overlay](Maven-Overlay-Installation.html) guide
describes how to build and deploy CAS. 

## Embedded

Note that CAS itself ships with aa number of embedded containers that allows the platform to be self contained as much as possible. You **DO
NOT** need to configure and deploy to an externally configured container. 

<div class="alert alert-info"><strong>Do Less</strong><p>
Remember that most if not all aspects of the embedded container can be controlled via the CAS properties.
See <a href="Configuration-Properties.html#embedded-tomcat">this guide</a> for more info.</p></div>

To see the relevant list of CAS properties, 
please [review this guide](Configuration-Properties.html#embedded-container).

### Apache Tomcat

Note that by default, the embedded container attempts to enable the HTTP2 protocol.

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-webapp-tomcat</artifactId>
     <version>${cas.version}</version>
</dependency>
```

#### Root Deployments

By default, a CAS server is generally mounted onto the URL `/cas` as the default context path such that the final public-facing URL
would become `https://sso.example.org/cas`.

If and when you choose to deploy CAS at root and remove the default context path, CAS by default attempts to deploy a special
`RewriteValve` for the embedded container that knows how to reroute urls and such for backward compatibility reasons.
The [configuration of this valve](https://tomcat.apache.org/tomcat-8.0-doc/rewrite.html),
should it need to be extended, can be controlled via the configuration settings.

### Jetty

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-webapp-jetty</artifactId>
     <version>${cas.version}</version>
</dependency>
```

### Undertow

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-webapp-undertow</artifactId>
     <version>${cas.version}</version>
</dependency>
```

## External

A CAS deployment may be deployed to any number of external servlet containers. The container **MUST** support
the servlet specification `v3.1.x` at a minimum. In these scenarios, the following vanilla CAS web application
may be used, in the [WAR Overlay](Maven-Overlay-Installation.html) :

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-webapp</artifactId>
     <version>${cas.version}</version>
</dependency>
```

While there is no official project support, the following containers should be compatible with a CAS deployment:

* [Apache Tomcat](http://tomcat.apache.org/)
* [JBoss](http://www.jboss.org/)
* [Wildfly](http://wildfly.org/)
* [Undertow](http://undertow.io/)
* [Jetty](http://www.eclipse.org/jetty/)
* [GlassFish](http://glassfish.java.net/)
* [WebSphere](http://www.ibm.com/software/websphere/)

Refer to the servlet container's own documentation for more info.

### Async Support

In the event that an external container is used, you must ensure it's configured correctly to support asynchronous requests.
This is typically handled by setting `<async-supported>true</async-supported>` inside the container's main `web.xml`  file.

## Docker

You may also be interested to deploy CAS via [Docker](https://www.docker.com/).
See [this guide](Docker-Installation.html) for more info.
