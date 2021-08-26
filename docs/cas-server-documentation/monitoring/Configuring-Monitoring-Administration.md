---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS Spring Boot Administration

CAS takes advantage of the [Spring Boot Admin][bootadmindocs] to manage and monitor its 
internal state visually. As a Spring Boot Admin client, CAS registers itself with the 
Spring Boot Admin server over HTTP and reports back its status and health to the server's web interface.

## Administration Server

The Spring Boot Admin web application server is not part of the CAS server and
is a standalone web application which can be deployed using the [CAS Initializr](../installation/WAR-Overlay-Initializr.html).

<div class="alert alert-warning"><strong>Secure Endpoints</strong><p>Note that the admin server's API 
endpoints MUST be secured. It is also best to run both the Admin server and the registering CAS server 
node under HTTPS, specially if credentials are used to authenticate into endpoints.</p></div>

To learn more about options, please [see this guide][bootadmindocs].

## CAS Server as Client

Each individual CAS server is given the ability to auto-register itself 
with the admin server, provided configuration is made available to instruct 
the CAS server how to locate and connect to the admin server.

Support is added by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-bootadmin-client" %}

Note that CAS server's actuator endpoints are by default secured. In order to allow secure 
communication between the CAS server and the Spring Boot Admin server, [please see guide][bootadmindocs].

## Configuration

{% include casproperties.html thirdParty="spring.boot.admin.client" %}

[bootadmindocs]: https://codecentric.github.io/spring-boot-admin/current/
