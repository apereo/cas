---
layout: default
title: CAS - Overlay Installation
---

# Servlet Container Configuration

The [WAR Overlay](Maven-Overlay-Installation.html) guide describes to build and deploy CAS. To see the relevant list of CAS properties, 
please [review this guide](Configuration-Properties.html).

## Embedded

Note that CAS itself ships with an embedded Tomcat container that allows the platform to be self contained as much as possible. You **DO 
NOT** need to configure and deploy to an externally configured container. 

<div class="alert alert-info"><strong>Note</strong><p>
Remember that mostly all aspects of the embedded container can be controlled via the CAS properties.
</p></div>

Other embedded containers such as Jetty and Undertow may also be configured as a Tomcat replacement. To do this, the provided
tomcat dependency **MUST** be excluded from the final package and substituted with either 
`org.springframework.boot:spring-boot-starter-undertow` 
or `org.springframework.boot:spring-boot-starter-jetty` instead.

### Root Deployments

By default, a CAS server is generally mounted onto the URL `/cas` as the default context path, such the final public-facing URL
would become `https://sso.example.org/cas`. 

If and when you choose to deploy CAS at root and remove the default context path, CAS by default attempts to deploy a special 
`RewriteValve` for the embedded container that knows how to reroute urls and such for backward compatibility reasons.
The configuration of this valve, should it need to be extended, can be controlled via 
the `server.tomcat.valve.rewrite.config` property.
 
## External
 
Optionally a CAS deployment may be deployed to any number of external servlet containers. The container **MUST** support
the servlet specification `v3.1.x` at a minimum.

While there is no official support, the following containers are compatible with a CAS deployment:

* [Apache Tomcat](http://tomcat.apache.org/)
* [JBoss](http://www.jboss.org/)
* [Undertow](http://undertow.io/)
* [Jetty](http://www.eclipse.org/jetty/)
* [GlassFish](http://glassfish.java.net/)
* [WebSphere](http://www.ibm.com/software/websphere/)

Refer to the servlet container's own documentation for more info.
