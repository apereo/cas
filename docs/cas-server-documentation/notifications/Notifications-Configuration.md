---
layout: default
title: CAS - Notifications
category: Notifications
---

{% include variables.html %}

# Notifications

CAS presents the ability to notify users and accounts on select actions via platform-specific notifications. Example actions include 
notification of risky authentication attempts or password reset links/tokens or one-time tokens for multifactor authentication. Providers 
and platforms supported by CAS are listed below. Note that an active/professional subscription may be required for certain providers.

## Google Firebase Cloud Messaging

Support is enabled via the relevant modules using the following module:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-notifications-fcm" %}

{% include casproperties.html properties="cas.google-firebase-messaging" %}
