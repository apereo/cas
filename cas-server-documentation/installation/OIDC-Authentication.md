---
layout: default
title: CAS - OIDC Authentication
---

# OpenID Connect Authentication

## Configuration
Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-oidc</artifactId>
  <version>${cas.version}</version>
</dependency>
```

## Register Clients

OpenID Connect clients can be registered with CAS as such:

```json
{
  "@class" : "org.jasig.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "signIdToken": true,
  "jwks": "..."
}
```

| Field                                | Description
|--------------------------------------+-----------------------------------------------------------------+
| `serviceId`                   | The redirect URI for this OIDC client.
| `signIdToken`                 | Whether ID tokens should be signed.
| `jwks`                        | Path to the location of the keystore that holds the signing keys. If none defined, default below will be 
used.

## Settings

The following settings are available:

```properties
# cas.oidc.issuer=${server.prefix}/oidc
# cas.oidc.jwks=file:/etc/cas/keystore.jwks
# cas.oidc.skew=5
```
