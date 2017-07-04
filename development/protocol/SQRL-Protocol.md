---
layout: default
title: CAS - SQRL Authentication
---

# SQRL Authentication

Verify and authenticate credentials using the [SQRL protocol](https://www.grc.com/sqrl/sqrl.htm).

In summary, the CAS login presents a QR code containing the URL of its authentication service, plus a non and sends it to the matching public key to identify the user, and the signature to authenticate it.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>This module is in an experimental state. The SQRL protocol is still under development and CAS support will eventually improve once the protocol state is finalized.</p></div>

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-sqrl</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html#sqrl-protocol).
