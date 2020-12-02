---
layout: default
title: CAS - Inwebo Authentication
category: Multifactor Authentication
---

# Inwebo Authentication

You can secure your CAS server with a second factor provided by [Inwebo](https://www.inwebo.com).

Apart from this CAS integration, notice that this identity provider can provide more general authentication solutions.

Support is enabled by including the following module in the overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-inwebo-mfa</artifactId>
     <version>${cas.version}</version>
</dependency>
```

The integration adds support for both push mobile/desktop and browser authentications.

## Configuration

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#inwebo).
