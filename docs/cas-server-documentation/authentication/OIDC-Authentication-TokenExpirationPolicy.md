---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# Token Expiration Policy - OpenID Connect Authentication

The expiration policy for OpenID Connect tokens is controlled by CAS settings and properties. These settings generally are the same as those that are 
defined for the OAuth Protocol. Expiration policies may also be controlled and defined for each application registered with CAS. 

[See this guide](OAuth-Authentication-TokenExpirationPolicy.html) for more info.
      
## ID Tokens

{% include_cached casproperties.html properties="cas.authn.oidc.id-token" %}
         
### Per Service

The expiration policy of certain ID tokens can be conditionally decided on a per-application basis. The candidate 
service whose id token expiration policy is to deviate from the default configuration must be designed as the following snippets demonstrate.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "Service",
  "id" : 1,
  "idTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOidcIdTokenExpirationPolicy",
    "timeToLive": "PT60S"
  }
}
```
