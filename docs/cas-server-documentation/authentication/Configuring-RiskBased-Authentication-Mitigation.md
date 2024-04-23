---
layout: default
title: CAS - Adaptive Risk-based Authentication
category: Authentication
---
{% include variables.html %}

# Risk Mitigation

Once an authentication attempt is deemed risky, a contingency plan may be enabled to mitigate risk. If configured and allowed,
CAS may notify both the principal and deployer via both email and sms.

Policy decisions include the following categories:

- You may decide to prevent the authentication flow to proceed and disallow the establishment of the SSO session.
- You may force the authentication event into a [multifactor flow of choice](../mfa/Configuring-Multifactor-Authentication.html),
  identified by the provider id.

{% include_cached casproperties.html properties="cas.authn.adaptive.policy" %}

## Messaging & Notifications
              
You may optionally decide to notify both the principal and deployer about the CAS policy decision when it reacts and responds to
a risky authentication attempt.

{% include_cached casproperties.html properties="cas.authn.adaptive.risk.response" includes=".mail,.sms" %}

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html).
