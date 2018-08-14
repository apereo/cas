---
layout: default
title: CAS - OAuth User-Managed Access Protocol
---

# User-Managed Access Protocol

User-Managed Access (UMA) is a lightweight access control protocol that defines a centralized workflow to allow an entity (user or corporation) 
to manage access to their resources.

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-oauth-uma</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties for this feature, please [review this guide](Configuration-Properties.html#oauth2-uma).
