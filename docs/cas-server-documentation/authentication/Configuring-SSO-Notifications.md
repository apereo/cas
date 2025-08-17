---
layout: default
title: CAS - Configuring SSO Sessions
category: SSO & SLO
---
{% include variables.html %}

# SSO Session - Notifications

CAS may be allowed to notify users when single sign-on sessions are established. Notifications are typically sent via email, but other channels such as SMS may also be used. The notification is sent to the user after a successful login, and the email template may contain information about the session, such as the time of login, the IP address, and the user agent, etc. 

{% include_cached casproperties.html properties="cas.sso" includes=".mail,.sms" %}
