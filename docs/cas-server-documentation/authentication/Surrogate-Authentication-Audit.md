---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}

# Audits - Surrogate Authentication

Surrogate authentication events are by default tracked in the audit logs:

```
=============================================================
WHO: (Primary User: [casuser], Surrogate User: [testuser])
WHAT: ST-1-u_R_SyXJJlENS0fBLwpecNE for https://example.app.edu
ACTION: SERVICE_TICKET_CREATED
APPLICATION: CAS
WHEN: Mon Sep 11 12:55:07 MST 2017
CLIENT IP ADDRESS: 127.0.0.1
SERVER IP ADDRESS: 127.0.0.1
=============================================================
```

Additionally, failure and success events may also communicated via SMS and/or email messages to relevant parties.

{% include_cached casproperties.html properties="cas.authn.surrogate" includes=".mail,.sms" %}

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html).
