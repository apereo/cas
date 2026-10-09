---
layout: default
title: CAS - Logout - REST Protocol
category: Protocols
---

{% include variables.html %}

# Logout - REST Protocol

End a single sign-on session through the REST API by deleting its ticket-granting ticket.

Destroy the SSO session by removing the issued ticket:

```bash
DELETE /cas/v1/tickets/TGT-fdsjfsdfjkalfewrihfdhfaie HTTP/1.0
```
