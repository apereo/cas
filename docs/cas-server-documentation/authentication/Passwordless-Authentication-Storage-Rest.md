---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# REST Passwordless Authentication Storage

This strategy allows one design REST endpoints in charge of locating 
passwordless user records. A successful execution of the endpoint  
would produce a response body similar to the following:

```json
{
  "@class" : "org.apereo.cas.api.PasswordlessUserAccount",
  "username" : "casuser",
  "email" : "cas@example.org",
  "phone" : "123-456-7890",
  "name" : "CASUser",        
  "multifactorAuthenticationEligible": "FALSE",  
  "delegatedAuthenticationEligible": "FALSE",  
  "requestPassword": false,
  "attributes":{ "lastName" : ["...", "..."] }
}
```

{% include_cached casproperties.html properties="cas.authn.passwordless.accounts.rest" %}
