---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# Mailjet SMS Messaging

To learn more, [Mailjet](https://www.mailjet.com/). The CAS integration with Mailjet to support SMS messages
and phone calls via dedicated configuration settings.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mailjet" %}
       
{% include_cached casproperties.html properties="cas.sms-provider.mailjet" %}
