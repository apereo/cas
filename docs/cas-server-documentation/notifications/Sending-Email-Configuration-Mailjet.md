---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email - Mailjet
   
You may instruct CAS to use [Mailjet](https://www.mailjet.com/) for sending emails.
Support is enabled by including the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mailjet" %}

{% include_cached casproperties.html properties="cas.email-provider.mailjet" displayEmailServers="false" %}
