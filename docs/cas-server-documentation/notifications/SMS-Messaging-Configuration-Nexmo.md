---
layout: default
title: CAS - Vonage (Nexmo) SMS Messaging
category: Notifications
---

{% include variables.html %}

# Vonage (Nexmo) SMS Messaging

Send SMS messages through the Vonage SMS API, formerly known as Nexmo. The module and its settings still use the `nexmo` name.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sms-nexmo" %}

{% include_cached casproperties.html properties="cas.sms-provider.nexmo" %}
