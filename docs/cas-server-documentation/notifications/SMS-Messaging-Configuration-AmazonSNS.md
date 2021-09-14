---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# Amazon SNS SMS Messaging

To learn more, [visit this site](https://docs.aws.amazon.com/sns).

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sms-aws-sns" %}

{% include_cached casproperties.html properties="cas.sms-provider.sns" %}
