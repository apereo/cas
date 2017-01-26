---
layout: default
title: CAS - JWT Service Tickets
---

# JWT Service Tickets

[JSON Web Tokens](http://jwt.io/) are an open, industry standard RFC 7519 method for representing claims securely between two parties.
CAS may also be allowed to fully create signed/encrypted JWTs and pass them back to the application in form of service tickets.
JWTs are entirely self-contained and contain the authenticated principal as well as all authorized attributes
in form of JWT claims.

## Overview

JWT-based service tickets are issued to application based on the same semantics defined by the [CAS Protocol](../protocol/CAS-Protocol.html).
CAS having received an authentication request via its `/login` endpoint, will conditionally issue back service tickets to the application
via the pre-defined `ticket` parameter via the requested http method. All JWTs are by default signed and encrypted by CAS based on keys
generated and controlled during deployment. Such keys may be exchanged with client applications to unpack the JWT and access claims.

## Configuration

JWT support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-token</artifactId>
     <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#jwt-service-tickets).

Signal the relevant application in CAS service registry to produce JWTs for service tickets:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.*",
  "name" : "Sample",
  "id" : 10,
  "properties" : {
    "@class" : "java.util.HashMap",
    "jwtAsResponse" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "true" ] ]
    }
  }
}
```
