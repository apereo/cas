---
layout: default
title: CAS - SCIM Provisioning
category: Integration
---

{% include variables.html %}

# SCIM Principal Provisioning

The SCIM standard is created to simplify user management and provisioning in the cloud by
defining a schema for representing users and groups and a REST API for all the necessary CRUD operations. SCIM 
integrations with CAS allow deployers to auto-provision the authenticated CAS principal to a SCIM server/target 
that supports the v2 specification of the SCIM protocol with additional support to map principal 
attributes into the appropriate claims and properties of the user resource.

Typical use case for enabling SCIM is to synchronize and provision user accounts, just in time, 
to services and applications that are integrated with CAS for single sign-on. In cases where 
the application also has its own account store, a mapping of user accounts between 
the CAS canonical account store (LDAP, JDBC, etc) and the application may be required. To 
accommodate this issue, CAS may be allowed to provision the authenticated principal 
via SCIM to a provisioning/identity/entity engine which would then dynamically synchronize user profiles to target systems.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-scim" %}

{% include_cached casproperties.html properties="cas.scim" %}

## Mapping Attributes

SCIM user resources are populated from CAS authenticated principals using one-to-one mapping rules. For example, the `givenName`
attribute in the SCIM schema is mapped and populated from the `givenName` attributes found for the CAS principal.

The set of attributes that are mapped are as follows:

| Attribute        | Description                                      |
|------------------|--------------------------------------------------|
| `userName`       | Set to the principal id.                         |
| `password`       | Set to the credential password, if available.    |
| `nickName`       | Set to the principal attribute `nickName`.       |
| `displayName`    | Set to the principal attribute `displayName`.    |
| `givenName`      | Set to the principal attribute `givenName`.      |
| `familyName`     | Set to the principal attribute `familyName`.     |
| `middleName`     | Set to the principal attribute `middleName`.     |
| `email`          | Set to the principal attribute `email`.          |
| `phoneNumber`    | Set to the principal attribute `phoneNumber`.    |
| `externalId`     | Set to the principal attribute `externalId`.     |
| `entitlements`   | Set to the principal attribute `entitlements`.   |
| `roles`          | Set to the principal attribute `roles`.          |
| `addresses`      | Set to the principal attribute `addresses`.      |
| `ims`            | Set to the principal attribute `ims`.            |
| `employeeNumber` | Set to the principal attribute `employeeNumber`. |
| `costCenter`     | Set to the principal attribute `costCenter`.     |
| `division`       | Set to the principal attribute `division`.       |
| `department`     | Set to the principal attribute `department`.     |
| `organization`   | Set to the principal attribute `organization`.   |
| `resourceType`   | Set to the principal attribute `resourceType`.   |
| `groups`         | Set to the principal attribute `groups`.         |

The default mapping rules can be controlled and customized using CAS properties. 

If the default mapping rules are not suitable, the mapping rules can always be adjusted 
and customized using the following bean:   

```java
@Bean
public ScimPrincipalAttributeMapper scim2PrincipalAttributeMapper() {
    return new MyPrincipalAttributeMapper();
}
```
  
## Mapping Groups

CAS can also provision groups to SCIM targets. The groups are populated from the principal attribute `groups`.
Memberships can be dynamically created, updated or removed based on the principal's group memberships.

## Per Application

SCIM relevant settings can be specified per application in form of service properties. 

{% include_cached registeredserviceproperties.html groups="SCIM" %}
 
A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 1,
  "properties" : {
    "@class" : "java.util.HashMap",
    "scimUsername" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "username" ] ]
    },
    "scimPassword" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "p@$$w0rd" ] ]
    }
  }
}
```
