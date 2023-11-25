---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# JWKS Storage - OpenID Connect Authentication

By default, a global keystore can be expected and defined via CAS properties as a path on the file system. The format 
of the keystore file is similar to the following:

```json
{
  "keys": [
    {
      "d": "...",
      "e": "AQAB",
      "use": "sig",
      "n": "...",
      "kty": "RSA",
      "kid": "cas",
      "state": 0
    }
  ]
}
```
 
The contents of the keystore may be encrypted via CAS configuration security [outlined here](../configuration/Configuration-Properties-Security-CAS.html).

<div class="alert alert-info">:information_source: <strong>Clustered Deployments</strong><p>
When deploying CAS in a cluster, you must make sure all CAS server nodes have access to 
and share an <strong>identical and exact copy</strong> of the keystore file. Keystore differences
will lead to various validation failures and application integration issues.
</p></div>

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.file-system" %}

The keystore is automatically watched and monitored by CAS for changes. As changes are detected, CAS
will invalidate the cache and will reload the keystore once again.

## Advanced

The following alternative strategies can be used to generate, manage and storage a JSON Web keystore.

| Option  | Reference                                                       |
|---------|-----------------------------------------------------------------|
| Groovy  | [See this page](OIDC-Authentication-JWKS-Storage-Groovy.html).  |
| JPA     | [See this page](OIDC-Authentication-JWKS-Storage-JPA.html).     |
| MongoDb | [See this page](OIDC-Authentication-JWKS-Storage-MongoDb.html). |
| REST    | [See this page](OIDC-Authentication-JWKS-Storage-REST.html).    |
| Custom  | [See this page](OIDC-Authentication-JWKS-Storage-Custom.html).  |

