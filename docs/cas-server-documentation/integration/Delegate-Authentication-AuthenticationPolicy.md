---
layout: default
title: CAS - Delegate Authentication Authentication Policy
category: Authentication
---

{% include variables.html %}

# Delegated Authentication - Authentication Policy

Service definitions may be conditionally authorized to use an external identity provider
by defining their own access strategy and authentication policy:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "delegatedAuthenticationPolicy" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
      "allowedProviders" : [ "java.util.ArrayList", [ "Facebook", "Twitter" ] ],
      "permitUndefined": true,
      "exclusive": false,
      "selectionStrategy": "file:/path/to/script.groovy"
    }
  }
}
```
     
The following fields are supported for the authentication policy:

| Type                | Description                                                                                                                                |
|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `allowedProviders`  | The list of allowed providers should contain the external identity provider names (i.e. client names).                                     |
| `permitUndefined`   | Decides whether access should be granted in the event that no allowed providers are defined explicitly.                                    |
| `exclusive`         | Decides whether authentication should be exclusively limited to allowed providers, disabling other methods such as username/password, etc. |
| `selectionStrategy` | Decides how to [select and redirect](Delegate-Authentication-Redirection.html) to the identity provider in a scripted fashion.             |

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.core" %}

## Auto Redirection

[See this guide](Delegate-Authentication-Redirection.html) for better details.
