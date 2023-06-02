---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}


# OAuth Authentication - JWT Access Tokens

By default, OAuth access tokens are created as opaque identifiers. There is 
also the option to generate JWTs as access tokens on a per-service basis:

```json
{
    "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
    "clientId": "clientid",
    "clientSecret": "clientSecret",
    "serviceId" : "^(https|imaps)://<redirect-uri>.*",
    "name" : "OAuthService",
    "id" : 100,
    "jwtAccessToken": true,
    "properties" : {
      "@class" : "java.util.HashMap",
      "accessTokenAsJwtSigningKey" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "..." ] ]
      },
      "accessTokenAsJwtEncryptionKey" : {
           "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
           "values" : [ "java.util.HashSet", [ "..." ] ]
      },
      "accessTokenAsJwtSigningEnabled" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "true" ] ]
      },
      "accessTokenAsJwtEncryptionEnabled" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "true" ] ]
      },
      "accessTokenAsJwtCipherStrategyType" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "ENCRYPT_AND_SIGN" ] ]
      }
    }
}
```

Signing and encryption keys may also be defined on a per-service basis, or globally via CAS settings.

{% include_cached registeredserviceproperties.html groups="JWT_ACCESS_TOKENS" %}
