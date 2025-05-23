---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - User Selection Menu

A passwordless account can be decorated to allow the user to select from a menu of available authentication options.
This feature is useful in scenarios where the account may be eligible for multiple forms of authentication
and the user is allowed to choose the most appropriate one. The passwordless account store is ultimately responsible
for determining the list of available authentication options and whether the user does quality for the authentication 
selection flow.

As an example, the following account managed via the 
[JSON passwordless account storage](Passwordless-Authentication-Storage-JSON.html) 
instructs CAS using the `allowSelectionMenu` flag to 
allow the flow to present available forms of authentication to the user:

```json
{
  "@class" : "java.util.LinkedHashMap",
  "casuser" : {
    "@class" : "org.apereo.cas.api.PasswordlessUserAccount",
    "username": "casuser",
    "name" : "CAS",
    "email": "casuser@example.org"
    "allowSelectionMenu": true,
    "requestPassword": true
    "delegatedAuthenticationEligible": "TRUE",
    "allowedDelegatedClients" : [ "java.util.ArrayList", [ "ExternalIdP" ] ]
    "multifactorAuthenticationEligible": "TRUE",
    "attributes" : {
      "@class" : "java.util.TreeMap",
      "memberOf" : [ "java.util.ArrayList", [ "mfa" ] ],
      "email": [ "java.util.ArrayList", [ "casuser@example.org" ] ]
    }
  }
}
```
  
The above account allows CAS to choose between the following options:

- Authentication via password.
- [Delegated authentication](../integration/Delegate-Authentication.html) via the `ExternalIdP` identity provider.
- [Multifactor authentication](../mfa/Configuring-Multifactor-Authentication.html).
- Passwordless [token-based authentication](Passwordless-Authentication.html).
