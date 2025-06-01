---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return Environment

Return environment info and application profiles to the service.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class": "org.apereo.cas.services.ReturnEnvironmentAttributeReleasePolicy",
    "environmentVariables": {
        "@class": "java.util.LinkedHashMap",
        "HOME": "MY_HOME"
    },
    "systemProperties": { 
        "@class": "java.util.LinkedHashMap", 
        "KEY": "MY_KEY" 
    }
  }
}
```
      
The above definition will fetch the environment variable `HOME` and release it as `MY_HOME`. Likewise,
it would fetch the system property `KEY` and release it as `MY_KEY`. Active application profiles 
are always released under an `applicationProfile` attribute.
