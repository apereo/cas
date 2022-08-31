---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Account Unlock - Password Management

In the event that the authentication source has determine the account status to either be locked or disabled, the
password management facility in CAS is able to intercept that flow automatically and allow the user to unlock and enable their account. 
The account unlock flow is fairly simple and modest and provides a *reCAPTCHA-like* unlocking mechanism. Additional modifications
and steps in this flow will require futher customizations. The unlocking of the account is ultimately passed onto the
account storage service and depends on whether the storage service (JSON, LDAP, etc) supports this capability. 
