---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# Twilio Messaging & Calls

To learn more, [visit this site](https://www.twilio.com/). The CAS integration with Twilio to support SMS messages
and phone calls via dedicated configuration settings.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-twilio" %}
       
{% include_cached casproperties.html properties="cas.sms-provider.twilio" %}
