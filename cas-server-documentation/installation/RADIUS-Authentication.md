---
layout: default
title: CAS - RADIUS Authentication
---

# RADIUS Authentication
RADIUS support is enabled by only including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-radius</artifactId>
  <version>${cas.version}</version>
</dependency>
```

RSA RADIUS support for MFA is enabled by only including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-radius-mfa</artifactId>
  <version>${cas.version}</version>
</dependency>
```

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).
