---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email - Mailgun
   
You may instruct CAS to use [Mailgun](https://www.mailgun.com/) for sending emails.
Support is enabled by including the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mailgun" %}

{% include_cached casproperties.html properties="cas.email-provider.mailgun" displayEmailServers="false" %}
