---
layout: default
title: CAS - Account Registration
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - Communication

Account creation requests are expected to be verified using a dedicated activation link that 
can be shared with the user using mail or text messages. The activation link is expected 
to remain valid for a configurable period of time.

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html), or [this guide](../notifications/Notifications-Configuration.html).

{% include_cached casproperties.html properties="cas.account-registration" includes=".mail,.sms" %}

