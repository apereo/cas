---
layout: default
title: CAS - JWT Authentication
category: Authentication
---
{% include variables.html %}

# JWT Authentication

JSON Web Tokens are an open, industry standard RFC 7519 method for representing claims securely between two parties.
CAS provides support for token-based authentication on top of JWT, where an authentication request can be granted an SSO session based
on a form of credentials that are JWTs.

## JWT Service Tickets

CAS may also be allowed to fully create signed/encrypted JWTs and pass them back to the application in form of service tickets.
In this case, JWTs are entirely self-contained and contain the authenticated principal as well as all authorized attributes
in form of JWT claims. To learn more about this functionality, [please review this guide](../installation/Configure-ServiceTicket-JWT.html).

## Overview

CAS expects a `token` parameter (or request header) to be passed along to the `/login` endpoint. The parameter value must be a JWT.

<div class="alert alert-info">:information_source: <strong>JCE Requirement</strong><p>It's safe to make sure you have 
the proper JCE bundle installed in your Java environment that is used by CAS, specially if you need to use specific 
signing/encryption algorithms and methods. Be sure to pick the right version of the JCE for your Java version. Java 
versions can be detected via the <code>java -version</code> command.</p></div>

The token must be signed with the signing secret of the service, and may be encrypted with its encryption secret.
Here is an example of how to generate a JWT with [Nimbus JOSE + JWT](https://connect2id.com/products/nimbus-jose-jwt):

```java
var signingSecret = RandomUtils.randomAlphanumeric(256);
var encryptionSecret = RandomUtils.randomAlphanumeric(48);

IO.println("signingSecret " + signingSecret);
IO.println("encryptionSecret " + encryptionSecret);

var claims = new JWTClaimsSet.Builder().subject("casuser").issueTime(new Date()).build();
var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
jwt.sign(new MACSigner(signingSecret.getBytes(StandardCharsets.UTF_8)));

var header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A192CBC_HS384).contentType("JWT").build();
var jwe = new JWEObject(header, new Payload(jwt));
jwe.encrypt(new DirectEncrypter(encryptionSecret.getBytes(StandardCharsets.UTF_8)));
var token = jwe.serialize();
IO.println("token: " + token);
```

The subject of the token becomes the authenticated principal, and its other claims become principal attributes.
Tokens built with the pac4j `JwtGenerator`, whose subject carries the pac4j profile type
(`org.pac4j.core.profile.CommonProfile#casuser`), are accepted as well. A token that is not signed, has expired or
has no subject is refused.

Once the token is generated, you may pass it to the `/login` endpoint of CAS as such:

```bash
/cas/login?service=https://...&token=<TOKEN_VALUE>
```

The `token` parameter may also be passed as a request header.

## Configuration

JWT authentication support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-token-webflow" %}

{% include_cached casproperties.html properties="cas.authn.token" %}

Configure the appropriate service in your service registry to hold the secrets:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://.+",
  "name" : "testId",
  "id" : 1,
  "properties" : {
    "@class" : "java.util.HashMap",
    "jwtSigningSecret" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "<SECRET>" ] ]
    },
    "jwtEncryptionSecret" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "<SECRET>" ] ]
    },
    "jwtSigningSecretAlg" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "HS256" ] ]
    },
    "jwtEncryptionSecretAlg" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "dir" ] ]
    },
    "jwtEncryptionSecretMethod" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "A192CBC-HS384" ] ]
    },
    "jwtSecretsAreBase64Encoded" : {
       "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
       "values" : [ "java.util.HashSet", [ "false" ] ]
    }
  }
}
```

Note that the only required property is `jwtSigningSecret`: a service that does not define it neither accepts nor
issues tokens. Signing and encryption keys are defined per service, in its properties.

{% include_cached registeredserviceproperties.html groups="JWT_AUTHENTICATION" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="tokenAuth" %}

## OpenID Connect Tokens

CAS also allows OpenID Connect access tokens and ID tokens to be passed as the `token` parameter. The token is 
decoded and then fetched as a valid access token from the ticket registry,
allowing CAS establish an authenticated session and subsequently create a single sign-on session.

To learn more about OpenID Connect, [please review this guide](../authentication/OIDC-Authentication.html).
