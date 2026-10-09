---
layout: default
title: CAS - Sending Email - Twilio SendGrid
category: Notifications
---

{% include variables.html %}

# Sending Email - Twilio SendGrid

Send email from CAS through Twilio SendGrid instead of an SMTP server.

   
You may instruct CAS to use [Twilio SendGrid](https://sendgrid.com/) for sending emails.
Support is enabled by including the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sendgrid" %}

{% include_cached casproperties.html thirdPartyStartsWith="spring.sendgrid" displayEmailServers="false" %}
