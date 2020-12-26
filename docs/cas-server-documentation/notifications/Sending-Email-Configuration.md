---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email

CAS presents the ability to notify users on select actions via email messages. Example actions include notification 
of risky authentication attempts or password reset links/tokens, etc. Configuring an email provider (i.e. Amazon Simple Email Service )
is a matter of defining SMTP settings. Each particular feature in need of email functionality should be able to 
gracefully continue in case settings are not defined. 

Default support for email notifications is automatically enabled/included by the relevant modules using the following module:

{% include casmodule.html group="org.apereo.cas" module="cas-server-core-notifications" %}

You need not explicitly include this module in WAR Overlay configurations, except when there is a need to access components and APIs at compile-time. 

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#email-submissions).
