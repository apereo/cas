---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email - Twilio SendGrid
   
You may instruct CAS to use [Twilio SendGrid](https://sendgrid.com/) for sending emails.
Support is enabled by including the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sendgrid" %}

{% include_cached casproperties.html thirdPartyStartsWith="spring.sendgrid" displayEmailServers="false" %}
