---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# Ticket Validation - REST Protocol

Service ticket validation is handled through the [CAS Protocol](CAS-Protocol.html)
via any of the validation endpoints such as `/p3/serviceValidate`. 

```bash
GET /cas/p3/serviceValidate?service={service url}&ticket={service ticket}
``` 

## Unsuccessful Response

CAS will send a 400 Bad Request. If an incorrect media type is
sent, it will send the 415 Unsupported Media Type.
