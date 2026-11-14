# CAS Spring Boot Administration

Source: https://apereo.github.io/cas/development/monitoring/Configuring-SpringBootAdmin.html

CAS takes advantage of [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) to manage and monitor its internal state visually. As a Spring Boot Admin client, CAS registers itself with the Spring Boot Admin server over HTTP and reports back its status and health to the server’s web interface.

## Spring Boot Admin Server

The Spring Boot Admin web application server is part of the CAS server via a dedicated extension module. Support is added by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-bootadmin"
}
```

> **Secure Endpoints**
>
> Note that the admin server's API endpoints MUST be secured. It is also best to run both the Admin server and the registering CAS server node under HTTPS, specially if credentials are used to authenticate into endpoints.

Once deployed, the Spring Boot Admin dashboard is available under the `/sba` context path.

To learn more about options, please [see this guide](https://github.com/codecentric/spring-boot-admin).

The following settings and properties are available from the CAS configuration catalog:

- `spring.boot.admin.server.enabled` (Third party; type `Boolean`; default `true`): Enable Spring Boot Admin Server Default: true

## Spring Boot Admin Client

Each individual CAS server is given the ability to auto-register itself with the Spring Boot Admin server, provided configuration is made available to instruct the CAS server how to locate and connect to the admin server.

Note that CAS server’s actuator endpoints are by default secured. In order to allow secure communication between the CAS server and the Spring Boot Admin server, [please see guide](https://github.com/codecentric/spring-boot-admin).

The following settings and properties are available from the CAS configuration catalog:

- `spring.boot.admin.client.api-path` (Third party; type `String`; default `instances`): The admin rest-apis path.
- `spring.boot.admin.client.auto-deregistration` (Third party; type `Boolean`): Enable automatic deregistration on shutdown If not set it defaults to true if an active `CloudPlatform` is present;
- `spring.boot.admin.client.auto-registration` (Third party; type `Boolean`; default `true`): Enable automatic registration when the application is ready.
- `spring.boot.admin.client.connect-timeout` (Third party; type `Duration`; default `5000ms`): Connect timeout for the registration.
- `spring.boot.admin.client.enabled` (Third party; type `Boolean`; default `true`): Enable Spring Admin Client.
- `spring.boot.admin.client.instance.health-url` (Third party; type `String`): Client-health-URL to register with. Inferred at runtime, can be overridden in case the reachable URL is different (e.g. Docker). Must be unique all services registry.
- `spring.boot.admin.client.instance.management-base-url` (Third party; type `String`): Base url for computing the management-url to register with. The path is inferred at runtime, and appended to the base url.
- `spring.boot.admin.client.instance.management-url` (Third party; type `String`): Management-url to register with. Inferred at runtime, can be overridden in case the reachable URL is different (e.g. Docker).
- `spring.boot.admin.client.instance.metadata` (Third party; type `Map<String,String>`): Metadata that should be associated with this application
- `spring.boot.admin.client.instance.name` (Third party; type `String`; default `spring-boot-application`): Name to register with. Defaults to ${spring.application.name}
- `spring.boot.admin.client.instance.prefer-ip` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `false`): Should the registered urls be built with server.address or with hostname. @deprecated Use serviceHostType instead.
- `spring.boot.admin.client.instance.service-base-url` (Third party; type `String`): Base url for computing the service-url to register with. The path is inferred at runtime, and appended to the base url.
- `spring.boot.admin.client.instance.service-host-type` (Third party; type `ServiceHostType`; default `canonical-host-name`): Should the registered urls be built with server.address or with hostname.
- `spring.boot.admin.client.instance.service-path` (Third party; type `String`): Path for computing the service-url to register with. If not specified, defaults to "/"
- `spring.boot.admin.client.instance.service-url` (Third party; type `String`): Client-service-URL register with. Inferred at runtime, can be overridden in case the reachable URL is different (e.g. Docker).
- `spring.boot.admin.client.password` (Third party; type `String`): Password for basic authentication on admin server
- `spring.boot.admin.client.period` (Third party; type `Duration`; default `10000ms`): Time interval the registration is repeated
- `spring.boot.admin.client.read-timeout` (Third party; type `Duration`; default `5000ms`): Read timeout (in ms) for the registration.
- `spring.boot.admin.client.register-once` (Third party; type `Boolean`; default `true`): Enable registration against one or all admin servers
- `spring.boot.admin.client.url` (Third party; type `String[]`): The admin server urls to register at
- `spring.boot.admin.client.username` (Third party; type `String`): Username for basic authentication on admin server

## Security

Accessing the Spring Boot Admin Server will by default require a form-based user authentication. The credentials used to access this feature are those presented by Spring Security configuration:

The following settings and properties are available from the CAS configuration catalog:

- `spring.security.user.name` (Third party; type `String`; default `user`): Default user name.
- `spring.security.user.password` (Third party; type `String`): Password for the default user name.
- `spring.security.user.roles` (Third party; type `List<String>`): Granted roles for the default user name.

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
