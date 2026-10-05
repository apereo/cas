---
layout: default
title: CAS - Sending Email - Default
category: Notifications
---

{% include variables.html %}

# Sending Email - Default

By default, CAS sends email through an SMTP server using the JavaMail API.

The default strategy uses the `JavaMail` API which provides a platform-independent and 
protocol-independent framework to build mail and messaging applications, primarily using SMTP:

{% include_cached {{ version }}/email-notifications-configuration.md %}
 
