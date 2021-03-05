---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# JSON Passwordless Authentication Storage

This strategy allows one to locate user records via a JSON resource, as such:

```json 
{
  "@class" : "java.util.LinkedHashMap",
  "casuser" : {
    "@class": "org.apereo.cas.api.PasswordlessUserAccount",
    "username": "casuser",
    "attributes": {
        "@class": "java.util.LinkedHashMap",
        "name": [ "java.util.ArrayList", ["value"] ]
    },
    "multifactorAuthenticationEligible": "TRUE",
    "delegatedAuthenticationEligible": "TRUE",
    "requestPassword": false
  }
}
```

{% include casproperties.html properties="cas.authn.passwordless.accounts.json" %}
