---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# Response & Grant Types - OAuth Authentication

Every OAuth relying party must be defined as a CAS service:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "supportedGrantTypes": [ "java.util.HashSet", [ "...", "..." ] ],
  "supportedResponseTypes": [ "java.util.HashSet", [ "...", "..." ] ]
}
```

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>CAS today does not strictly enforce 
the collection of authorized supported response/grant types for backward compatibility reasons. This means that if left undefined, 
all grant and response types may be allowed by the service definition and related policies. Do please note that this behavior 
is <strong>subject to change</strong> in future releases and thus, it is strongly recommended that all authorized 
grant/response types for each profile be declared in the service definition immediately to avoid surprises in the future.</p></div>

## Supported Grant Types
         
The following grant types are supported by CAS:

| Grant Type                                        |
|---------------------------------------------------|
| `urn:ietf:params:oauth:grant-type:device_code`    |
| `authorization_code`                              |
| `password`                                        |
| `client_credentials`                              |
| `refresh_token`                                   |
| `urn:ietf:params:oauth:grant-type:uma-ticket`     |
| `urn:ietf:params:oauth:grant-type:token-exchange` |

## Supported Response Types

The following response types are supported by CAS:

| Grant Type       |
|------------------|
| `code`           |
| `token`          |
| `device_code`    |
| `id_token token` |
| `id_token`       |
