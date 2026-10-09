---
layout: default
title: CAS - Ticket Status - REST Protocol
category: Protocols
---

{% include variables.html %}

# Ticket Status - REST Protocol

Verify the status of an obtained ticket to make sure it still is valid
and has not yet expired.

```bash
GET /cas/v1/tickets/TGT-fdsjfsdfjkalfewrihfdhfaie HTTP/1.0
```

## Successful Response

```bash
200 OK
```

## Unsuccessful Response

```bash
404 NOT FOUND
```
