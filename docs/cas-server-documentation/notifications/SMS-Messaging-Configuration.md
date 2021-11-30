---
layout: default 
title: CAS - SMS Messaging 
category: Notifications
---

{% include variables.html %}

# SMS Messaging

CAS presents the ability to notify users on select actions via SMS messaging. Example actions include notification of risky authentication
attempts or password reset links/tokens. SMS providers supported by CAS are listed below. Note that an active/professional subscription may
be required for certain providers.

Default support for SMS notifications is automatically enabled/included by the relevant modules using the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-core-notifications" %}

You need not explicitly include this module in WAR Overlay configurations, except when there is a need to access components and APIs at
compile-time. See below on how to customize or override the default behavior with specific providers.

| Provider   | Reference                                                      |
|------------|----------------------------------------------------------------|
| Custom     | [See this guide](SMS-Messaging-Configuration-Custom.html).     |
| Groovy     | [See this guide](SMS-Messaging-Configuration-Groovy.html).     |
| REST       | [See this guide](SMS-Messaging-Configuration-REST.html).       |
| Twilio     | [See this guide](SMS-Messaging-Configuration-Twilio.html).     |
| TextMagic  | [See this guide](SMS-Messaging-Configuration-TextMagic.html).  |
| Clickatell | [See this guide](SMS-Messaging-Configuration-Clickatell.html). |
| SmsMode    | [See this guide](SMS-Messaging-Configuration-SmsMode.html).    |
| Amazon SNS | [See this guide](SMS-Messaging-Configuration-AmazonSNS.html).  |
| Nexmo      | [See this guide](SMS-Messaging-Configuration-Nexmo.html).      |
