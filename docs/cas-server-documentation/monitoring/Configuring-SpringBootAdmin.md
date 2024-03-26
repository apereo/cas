---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS Spring Boot Administration

CAS takes advantage of [Spring Boot Admin][bootadmindocs] to manage and monitor its 
internal state visually. As a Spring Boot Admin client, CAS registers itself with the 
Spring Boot Admin server over HTTP and reports back its status and health to the server's web interface.

## Spring Boot Admin Server

The Spring Boot Admin web application server is part of the CAS server via a dedicated extension module. Support 
is added by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-bootadmin" %}

<div class="alert alert-warning">:warning: <strong>Secure Endpoints</strong><p>Note that the admin server's API 
endpoints MUST be secured. It is also best to run both the Admin server and the registering CAS server 
node under HTTPS, specially if credentials are used to authenticate into endpoints.</p></div>

Once deployed, the Spring Boot Admin dashboard is available under the `/sba` context path.

To learn more about options, please [see this guide][bootadmindocs].

{% include_cached casproperties.html thirdPartyStartsWith="spring.boot.admin.server" %}

## Spring Boot Admin Client

Each individual CAS server is given the ability to auto-register itself 
with the Spring Boot Admin server, provided configuration is made available to instruct 
the CAS server how to locate and connect to the admin server.

Note that CAS server's actuator endpoints are by default secured. In order to allow secure 
communication between the CAS server and the Spring Boot Admin server, [please see guide][bootadmindocs].

{% include_cached casproperties.html thirdPartyStartsWith="spring.boot.admin.client" %}

[bootadmindocs]: https://github.com/codecentric/spring-boot-admin
         
## Security

Accessing the Spring Boot Admin Server will by default require a form-based user authentication. The credentials
used to access this feature are those presented by Spring Security configuration:

{% include_cached casproperties.html thirdPartyStartsWith="spring.security.user" %}
               
Additional points to consider:

- Spring Boot Admin Server will reach out to CAS actuator endpoints to parse and present data. As CAS actuator endpoints require authenticated access, Spring Boot Admin Server is configured by default to use the same credentials used to protected actuator endpoints.
- CAS server acting as a Spring Boot Admin client will reach out to the Spring Boot Admin Server to auto-register itself and report status updates. Such requests and API calls re by default configured to use the same security mechanism and credentials.
- CAS server can act both as a Spring Boot Admin Server or a client of the Spring Boot Admin Server. Each CAS server deployment can be individually tuned to turn off client/service functionality as needed. For example, in a clustered CAS deployment the primary CAS server node might act as both Spring Boot Admin Server and Client, while all other secondary nodes may simply be a client of the primary CAS (Spring Boot Admin) server.

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="de.codecentric.boot" level="debug" />
<Logger name="org.springframework.web.reactive" level="debug" />
```
