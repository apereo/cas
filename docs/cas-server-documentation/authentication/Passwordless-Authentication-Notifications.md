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
