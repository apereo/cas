---
layout: default
title: CAS - Notifications - Slack
category: Notifications
---

{% include variables.html %}

# Notifications - Slack

Send notifications to users as Slack messages.

Support is enabled via the relevant modules using the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-notifications-slack" %}

{% include_cached casproperties.html properties="cas.slack-messaging" %}
