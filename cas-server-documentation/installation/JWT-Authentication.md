---
layout: default
title: CAS - JWT Authentication
---

# JWT Authentication

[JSON Web Tokens](http://jwt.io/) are an open, industry standard RFC 7519 method for representing claims securely between two parties.
CAS provides support for token-based authentication on top of JWT, where an authentication request can be granted an SSO session based
on a form of credentials that are JWTs. 

## Overview

CAS expects a `token` parameter to be passed along to the `/login` endpoint. The parameter value must be a 
JWT. Here is an example of how to generate a JWT via [Pac4j](https://github.com/pac4j/pac4j):

```java
final String signingSecret = RandomStringUtils.randomAlphanumeric(256);
final String encryptionSecret = RandomStringUtils.randomAlphanumeric(48);

final JwtGenerator<CommonProfile> g = new JwtGenerator<>(signingSecret, encryptionSecret);

/*
// Use the same key for signing and encryption
final JwtGenerator<CommonProfile> g = new JwtGenerator<>(signingSecret);

// Do not execute encryption
final JwtGenerator<CommonProfile> g = new JwtGenerator<>(signingSecret, false);
 */

g.setEncryptionMethod(EncryptionMethod.A192CBC_HS384);

final CommonProfile profile = new CommonProfile();
profile.setId("<PRINCIPAL_ID>");
final String token = g.generate(profile);
System.out.println(token);
...
```

Once the token is generated, you may pass it to the `/login` endpoint of CAS as such:

```bash
/cas/login?service=https://...&token=<TOKEN_VALUE>
```

## Configuration

JWT authentication support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-token-webflow</artifactId>
     <version>${cas.version}</version>
</dependency>
```

Configure the appropriate service in your service registry to hold the secrets:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "https://.+",
  "name" : "testId",
  "id" : 1,
  "properties" : {
    "@class" : "java.util.HashMap",
    "jwtSigningSecret" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "<SIGNING_SECRET>" ] ]
    },
    "jwtEncryptionSecret" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "<ENCRYPTION_SECRET>" ] ]
    }
}
```

Note that the configuration of `jwtEncryptionSecret` is optional. 
