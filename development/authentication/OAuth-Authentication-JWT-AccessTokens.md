# OAuth Authentication - JWT Access Tokens

Source: https://apereo.github.io/cas/development/authentication/OAuth-Authentication-JWT-AccessTokens.html

By default, OAuth access tokens are created as opaque identifiers. There is also the option to generate JWTs as access tokens on a per-service basis:

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

Signing and encryption keys may also be defined on a per-service basis, or globally via CAS settings. A JWT access token must be signed or encrypted, with the keys of the service or of CAS: one that neither would sign nor encrypt would be an unsigned JWT, whose algorithm is `none`, which CAS neither issues nor accepts, whether as an access token, a refresh token, or a subject or actor token of a token exchange.

The following properties are available and recognized by CAS for various modules and features:

| Name | Default Value | Type | Group |
| --- | --- | --- | --- |
| `oidcResponseModeAsJwtCipherSigningEnabled` | `true` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `oidcResponseModeAsJwtCipherEncryptionEnabled` | `true` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtSigningKey` |  | `STRING` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtCipherStrategyType` | `ENCRYPT_AND_SIGN` | `STRING` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtSigningEnabled` | `true` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtEncryptionEnabled` | `false` | `BOOLEAN` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtEncryptionKey` |  | `STRING` | `JWT_ACCESS_TOKENS` |
| `accessTokenAsJwtEncryptionAlg` |  | `STRING` | `JWT_ACCESS_TOKENS` |
