---
layout: default
title: CAS - Sending Email
category: Notifications
---

{% include variables.html %}

# Sending Email - Microsoft Azure Active Directory

When using Microsoft’s SMTP service with OAuth2 (instead of a static password), this module allows CAS
to obtain an access token dynamically instead of storing and using a fixed password. The access token is acquired from 
Microsoft’s Identity platform using the XOAUTH2 authentication mechanism.

Support is enabled by including the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mail-microsoft" %}

{% include_cached casproperties.html
    properties="cas.email-provider.microsoft"
    displayEmailServers="true" %}
