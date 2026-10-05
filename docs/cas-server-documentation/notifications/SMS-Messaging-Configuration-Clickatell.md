---
layout: default
title: CAS - Clickatell SMS Messaging
category: Notifications
---

{% include variables.html %}

# Clickatell SMS Messaging

Send SMS messages from CAS through Clickatell.

To learn more, [visit this site](http://www.clickatell.com/).

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sms-clickatell" %}

{% include_cached casproperties.html properties="cas.sms-provider.clickatell" %}
