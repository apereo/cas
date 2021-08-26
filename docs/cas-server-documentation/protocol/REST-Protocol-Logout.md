---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# Logout - REST Protocol

Destroy the SSO session by removing the issued ticket:

```bash
DELETE /cas/v1/tickets/TGT-fdsjfsdfjkalfewrihfdhfaie HTTP/1.0
```
