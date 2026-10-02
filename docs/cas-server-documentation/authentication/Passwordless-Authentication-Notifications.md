---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - Messaging & Notifications

Users may be notified of tokens via text messages, mail, etc.
To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html).

{% include_cached casproperties.html properties="cas.authn.passwordless.tokens" includes=".mail,.sms" %}

## SMS One-Time Codes

CAS ends the SMS text with an [origin-bound one-time code](https://wicg.github.io/sms-one-time-codes/)
line, `@<host> #<token>`, where the host is taken from the CAS server name. For example, with `cas.server.name`
set to `https://sso.example.org` and a message text of `Your token is ${token}`, the user receives:

```
Your token is 123456

@sso.example.org #123456
```

Browsers and phones that understand this format offer the code only on that host and can fill it in automatically,
and the passwordless token page reads it through the [WebOTP API](https://wicg.github.io/web-otp/) where the browser
supports it, submitting the code without the user typing it. The code is bound to the host alone; ports are not part
of the format, so it does not apply to a CAS server name that includes a non-standard port.
