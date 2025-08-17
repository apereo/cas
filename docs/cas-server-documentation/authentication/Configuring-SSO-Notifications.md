---
layout: default
title: CAS - Configuring SSO Sessions
category: SSO & SLO
---
{% include variables.html %}

# SSO Session - Notifications

CAS may be allowed to notify users when single sign-on sessions are established. Notifications are typically sent via email, but other channels such as SMS may also be used. The notification is sent to the user after a successful login, and the email template may contain information about the session, such as the time of login, the IP address, and the user agent, etc. 

{% include_cached casproperties.html properties="cas.sso" includes=".mail,.sms" %}

To construct the notification template, the following variables are passed to the template:

| Parameter              | Description                                                               |
|------------------------|---------------------------------------------------------------------------|
| `requestContext`       | The object representing the Spring Webflow `RequestContext`.              |
| `token`                | A special JWT, signed and encrypted, that carries the ticket-granting id. |
| `ticketGrantingTicket` | The ticket-granting ticket id that represents the single sign-on session. |
| `clientInfo`           | The `ClientInfo` object that represents the client request.               |
| `authentication`       | The `Authentication` object that is linked to the single sign-on session. |
| `principal`            | The `Principal` object that is linked to the authentication attempt.      |
| `principalId`          | The authenticated user id, identical to `Principal#getId()`               |
