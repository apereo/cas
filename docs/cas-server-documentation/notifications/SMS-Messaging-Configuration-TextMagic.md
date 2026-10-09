---
layout: default
title: CAS - TextMagic SMS Messaging
category: Notifications
---

{% include variables.html %}

# TextMagic SMS Messaging

Send SMS messages from CAS through TextMagic.

To learn more, [visit this site](https://www.textmagic.com/).

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sms-textmagic" %}

{% include_cached casproperties.html properties="cas.sms-provider.text-magic" %}
