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
  "casuser@\w+.com" : {
    "@class": "org.apereo.cas.api.PasswordlessUserAccount",
    "username": "casuser",
    "attributes": {
        "@class": "java.util.LinkedHashMap",
        "name": [ "java.util.ArrayList", ["value"] ]
    },
    "multifactorAuthenticationEligible": "TRUE",
    "delegatedAuthenticationEligible": "TRUE",
    "allowedDelegatedClients" : [ "java.util.ArrayList", [ "ClientName1" ] ],
    "requestPassword": false,
    "allowSelectionMenu": false
  }
}
```

<div class="alert alert-info">:information_source: <strong>Note</strong><p>
Note that the key specified in the above block can be a regular expression pattern.
</p></div>

{% include_cached casproperties.html properties="cas.authn.passwordless.accounts.json" %}
