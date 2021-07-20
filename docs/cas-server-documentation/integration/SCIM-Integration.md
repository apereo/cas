---
layout: default
title: CAS - SCIM Provisioning Integration
category: Integration
---

{% include variables.html %}

# Overview

The [SCIM standard](http://www.simplecloud.info/) is created to simplify user management and provisioning in the cloud by
defining a schema for representing users and groups and a REST API for all the necessary CRUD operations. SCIM 
integrations with CAS allow deployers to auto-provision the authenticated CAS principal to a SCIM server/target 
with additional support to map principal attributes into the appropriate claims and properties of the user resource.

SCIM v2 is supported, thanks to the SDK provided by [UnboundID](https://github.com/PingIdentity).

<div class="alert alert-warning"><strong>SCIM v1 Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

Typical use case for enabling SCIM is to synchronize and provision user accounts, just in time, 
to services and applications that are integrated with CAS for single sign-on. In cases where 
the application also has its own account store, a mapping of user accounts between 
the CAS canonical account store (LDAP, JDBC, etc) and the application may be required. To 
accommodate this issue, CAS may be allowed to provision the authenticated principal 
via SCIM to a provisioning/identity/entity engine which would then dynamically synchronize user profiles to target systems.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-scim" %}

{% include casproperties.html properties="cas.scim" %}

## Per Application

SCIM relevant settings can be specified per application in form of service properties. 

{% include registeredserviceproperties.html groups="SCIM" %}
 
A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
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
