---
layout: default
title: CAS - Acceptto Authentication
category: Multifactor Authentication
---

# Acceptto Authentication

Secure your workforce identity with [Acceptto](https://www.acceptto.com) 
end-to-end risk-based multiFactor authentication.

Start by visiting the [Acceptto documentation](https://www.acceptto.com/acceptto-mfa-rest-api/).

Support is enabled by including the following module in the overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-acceptto-mfa</artifactId>
     <version>${cas.version}</version>
</dependency>
```

## Configuration

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#acceptto).
