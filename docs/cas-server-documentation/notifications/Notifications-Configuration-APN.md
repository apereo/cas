---
layout: default
title: CAS - Notifications - Apple Push Notification Messaging
category: Notifications
---

{% include variables.html %}

# Notifications - Apple Push Notification Messaging

Send push notifications to users' Apple devices through the Apple Push Notification service.

Support is enabled via the relevant modules using the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-notifications-apn" %}

{% include_cached casproperties.html properties="cas.apn-messaging" %}
