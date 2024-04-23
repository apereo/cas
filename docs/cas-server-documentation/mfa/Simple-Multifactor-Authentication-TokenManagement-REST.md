---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication - REST Token Management

Token validation and management can also be outsources to an external REST API. 

{% include_cached casproperties.html properties="cas.authn.mfa.simple.token.rest" %}

The API service is primarily response for two operations: issuing tokens so they may be 
shared with the end-user and validating tokens once the end-user provides them back to CAS.

## Generating Tokens

When tokens need to be generated, this API endpoint would be invoked via a `GET` to create the ticket identifier. The body of 
the request will contain the authenticated principal that is put through the multifactor authentication flow,
and the requesting application for which the token should be generated is passed to the API via a `service` parameter. 

The endpoint is expected to respond to token generation requests at a `/new` URL path suffix, and should produce a `2xx`
status code where the response body is expected to contain the token identifier.
                                                                 
## Storing Tokens

Generated tokens, that are shared with end-users, can be stored via the REST API endpoint using a `POST`. The body of the request
would contain the actual token definition and details that should be stored. The API service should produce a `2xx` 
status code on successful operations.

## Validating Tokens

Generated tokens are passed to this API to validation where the token is appended to the URL endpoint and acts as a path variable. The response 
that is returned to a `GET` call must be accompanied by a `2xx` status code where the body should contain `id` and `attributes` fields, the 
latter being optional, which represent the authenticated principal for CAS:

```json
{
  "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
  "id": "casuser",
  "attributes": {
    "@class": "java.util.LinkedHashMap",
    "names": [
      "java.util.List", ["cas", "user"]
    ]
  }
}
```

## Fetching Tokens

Generated tokens are passed to this API to be retrieved and queried where the token is appended to the URL endpoint and acts as a path variable. The response
that is returned to a `GET` call must be accompanied by a `2xx` status code where the body should contain `id` and `attributes` fields, the
latter being optional, which represent the authenticated principal for CAS:

```json
{
  "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
  "id": "casuser",
  "attributes": {
    "@class": "java.util.LinkedHashMap",
    "names": [
      "java.util.List", ["cas", "user"]
    ]
  }
}
```
