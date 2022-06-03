---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication - Communication Strategy

Users may be notified of CAS-issued tokens via text messages and/or email. The
authenticated CAS principal is expected to carry enough attributes, configurable via CAS settings, in order for CAS to properly send text messages
and/or email to the end-user. Tokens may also be shared via notification strategies back by platforms such as Google Firebase, etc.

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html), or [this guide](../notifications/Notifications-Configuration.html).

{% include_cached casproperties.html properties="cas.authn.mfa.simple.mail,cas.authn.mfa.simple.sms" %}
