---
layout: default
title: CAS - Sending Email - Amazon Simple Email Service (SES)
category: Notifications
---

{% include variables.html %}

# Sending Email - Amazon Simple Email Service (SES)

Send email from CAS through Amazon Simple Email Service instead of an SMTP server.

   
You may instruct CAS to use [Amazon SES](https://aws.amazon.com/ses/) for sending emails.
Support is enabled by including the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aws-ses" %}

{% include_cached casproperties.html properties="cas.email-provider.ses" displayEmailServers="false" %}
