---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - SCIM

The SCIM access strategy is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-scim" %}

{% include_cached casproperties.html properties="cas.scim" %}

This access strategy attempts to locate user groups for the CAS principal by querying for the user and extracting groups from 
the response. The groups are collected as CAS attributes under a `scimGroups` attribute and then examined against 
the list of required attributes for service access.

A sample access strategy based follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.scim.v2.access.ScimRegisteredServiceAccessStrategy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "scimGroups" : [ "java.util.HashSet", [ "..." ] ]
    }
  }
}
```
 
