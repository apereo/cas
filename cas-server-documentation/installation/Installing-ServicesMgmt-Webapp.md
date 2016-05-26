---
layout: default
title: CAS - Services Management Webapp
---
# Services Management Webapp

The services management webapp is no longer part of the CAS server and
is a standalone web application: `cas-management-webapp`.

* The management webapp is used to add/edit/delete all the CAS services
* The CAS server loads/relies on all these defined CAS services to process all incoming requests.

<div class="alert alert-warning"><strong>Synchronized Configuration</strong><p>
You MUST keep in mind that both applications (the CAS server and the services management webapp)
share the <strong>same</strong> configuration for the CAS services.
</p></div>

A sample Maven overlay for the services management webapp is provided here: [https://github.com/Apereo/cas-services-management-overlay](https://github.com/Apereo/cas-services-management-overlay)

## Services Registry

You also need to define the *common* services registry by overriding the `src/main/resources/managementConfigContext.xml`
file and set the appropriate `serviceRegistryDao`. The [persistence storage](Service-Management.html) MUST be the same.
It should be the same configuration you already use in your CAS server in the `/deployerConfigContext.xml` file.

## Authentication Method

By default, the `cas-management-webapp` is configured to authenticate against a CAS server. 

## Configuration
The following properties are applicable and must be adjusted by overriding the default `src/main/resources/cas-management.properties` file:

```properties
# CAS
cas.host=http://localhost:8080
cas.prefix=${cas.host}/cas
cas.securityContext.casProcessingFilterEntryPoint.loginUrl=${cas.prefix}/login

# Management
cas-management.host=${cas.host}
cas-management.prefix=${cas-management.host}/cas-management
cas-management.securityContext.serviceProperties.service=${cas-management.prefix}/callback

cas-management.securityContext.serviceProperties.adminRoles=ROLE_ADMIN
```

## Securing Access and Authorization
Access to the management webapp is controlled via pac4j. Rules are defined in 
the `src/main/resources/managementConfigContext.xml` file.


### Static List of Users
By default, access is limited to a static list of users whose credentials may be specified in a `user-details.properties` 
file that should be available on the runtime classpath. You can change the location of this file, by uncommenting the following key in your `cas-management.properties` file:

```properties
##
# User details file location that contains list of users
# who are allowed access to the management webapp:
#
# user.details.file.location = classpath:user-details.properties
```

The format of the file should be as such:

```bash
# The syntax of each entry should be in the form of:
#
# username=password,grantedAuthority[,grantedAuthority][,enabled|disabled]

# Example:
# casuser=notused,ROLE_ADMIN
```

### CAS ABAC

The following authorization generator examines the CAS response for attributes
and will grant access if an attribute name matches the value of `adminRoles` defined in the configuration.
 
```xml
<bean id="authorizationGenerator" class="org.pac4j.core.authorization.FromAttributesAuthorizationGenerator"
    c:roleAttributes="ROLE_ADMIN,ROLE_CUSTOM" c:permissionAttributes="CUSTOM_PERMISSION1,CUSTOM_PERMISSION2" />
```

### Custom ABAC

Define a custom set of roles and permissions that would be cross-checked later against the value of `adminRoles`
defined in the configuration.
 
```xml
<bean id="authorizationGenerator" class="org.pac4j.core.authorization.DefaultRolesPermissionsAuthorizationGenerator"
    c:defaultRoles="ROLE_ADMIN,ROLE_CUSTOM" c:defaultPermissions="CUSTOM_PERMISSION1,CUSTOM_PERMISSION2" />
```

### LDAP

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-management-webapp-support-ldap</artifactId>
  <version>${cas.version}</version>
</dependency>
```


The following properties are applicable to this configuration:

```properties
# ldap.url=ldap://localhost:1389
# ldap.baseDn=dc=example,dc=org
# ldap.user.searchFilter=cn={0}
#
# ldap.pool.blockWaitTime=5000
# ldap.pool.validateOnCheckout=true
# ldap.pool.validatePeriodically=true
# ldap.pool.validatePeriod=5000
# ldap.pool.idleTime=5000
# ldap.pool.maxSize=1
# ldap.pool.minSize=1
# ldap.pool.prunePeriod=10000
# ldap.connectTimeout=5000
# ldap.useStartTLS=false
# ldap.use.ssl=false
#
# ldap.authorizationgenerator.role.attribute=sn
# ldap.authorizationgenerator.role.prefix=ROLE_
# ldap.authorizationgenerator.allow.multiple=false
```
