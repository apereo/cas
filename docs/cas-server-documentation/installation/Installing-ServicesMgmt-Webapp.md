---
layout: default
title: CAS - Services Management Webapp
---
# Services Management Webapp

The services management webapp is no longer part of the CAS server and
is a standalone Spring Boot web application that ships with an embedded Apache Tomcat container.

* The management webapp is used to add/edit/delete all the CAS services.
* The CAS server loads/relies on all these defined CAS services to process all incoming requests.

<div class="alert alert-warning"><strong>Synchronized Configuration</strong><p>
You <strong>MUST</strong> keep in mind that both applications (the CAS server and the services management webapp)
share the <strong>same</strong> service registry configuration for CAS services.
</p></div>

A template overlay for the services management webapp is [provided here](https://github.com/apereo/cas-services-management-overlay).

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#management-webapp).

## Configuration

The management web application is primarily controlled by a `management.yml|properties` file. However, all strategies outlined in [CAS configuration management](Configuration-Management.html) equally apply here as well in the way that settings are defined, passed and resolved. The primary difference of course is the name of the configuration file.

## Services Registry

The [persistence storage](Service-Management.html) for services **MUST** be the same as that of the CAS server. The same service registry component that is configured for the CAS server, including module and settings, needs to be configured in the same exact way for the management web application.

## Authentication

Access to the management webapp can be configured via the following strategies.

### CAS Server

The management web application can be configured to authenticate against a CAS server. To activate this mode, simply specify the location of the CAS server via configuration settings. To disable this mode, blank out the settings that describe the external CAS server.

If this strategy is used, access strategy rules can then further be controlled via the outlined strategies for authorization.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#management-webapp).

### IP Address

The management web application can be configured to allow anonymous access if the request's IP address matches a predefined regular expression. To disable this mode, blank out the settings that describe the external CAS server. If this strategy is used, access strategy and authorized rules do not apply as the resolved identity is simply anonymous.

<div class="alert alert-danger"><strong>Be Careful</strong><p>Keep in mind that this authentication
mechanism should only be enabled for internal network clients with relatively static IP addresses.</p></div>

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#management-webapp).

### Anonymous

The management web application can be configured to allow anonymous access if no other authentication strategy is defined. This mode is mostly useful for development and testing while additional warnings show up in the logs that explain the caveats of this option.

<div class="alert alert-danger"><strong>Be Careful</strong><p>Be sure to specify an authentication strategy, as failure to do so would effectively leave the management web application open for access.</p></div>

## Authorization

Learn how to control access to the management web application.

### Static List of Users

By default, access is limited to a static list of users whose credentials may be
specified in a `user-details.properties` file that should be available on the runtime classpath.

### Attribute

Alternatively, the authorization generator examines the CAS validation response and principal for attributes
and will grant access if an attribute name matches the value of `adminRoles` defined in the configuration.

### LDAP

Access to the management web application may also be controlled directly by querying an LDAP server, via the following modes.

#### Groups

The authorization framework will search for groups of which the user is a member. Retrieved groups and roles are the compared with the management webapp configuration to find a match (i.e. `ROLE_ADMIN`).

#### Attributes

The authorization framework will examine the attributes assigned to the user, looking for a predefined role attribute to compare with the configuration for access (i.e. `ROLE_ADMIN`).

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-management-webapp-support-ldap</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#ldap-authorization).
