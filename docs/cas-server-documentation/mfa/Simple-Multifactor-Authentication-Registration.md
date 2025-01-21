---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication - Registration

By default, registration is expected to have occurred as an out-of-band process. Ultimately,
CAS expects to fetch the necessary attributes from configured attribute sources to determine communications channels for
email and/or SMS. The adopter is expected to have populated user records with enough information to indicate a phone number and/or email
address where CAS could then be configured to fetch and examine those attributes to share generated tokens.

In the event that no contact information can be found for the user, CAS will inform the user and allows
the flow to be redirected to an external application where onboarding and device registration is expected to occur.

## Just-In-Time Registration

CAS also supports a just-in-time registration strategy where users are allowed to register 
their email address for multifactor authentication. In the event that no contact information can be found for the user,
CAS will present an option for the user to register their email address. A one-time-token is then sent to the user's 
email address and upon verification, the user is allowed to proceed with the multifactor authentication flow.

<div class="alert alert-warning">:warning: <strong>Usage</strong><p>It goes without saying that
there are security considerations with the just-in-time device registration flow. When enabled,
system security and user protection is largely the responsibility of the initial authentication event
and the protection of primary user credentials.</p></div>

This option is explicitly turned off by default and can be enabled via CAS settings notably by defining
a regular expression that controls the format of the allowed email addresses that may be registered.

{% include_cached casproperties.html properties="cas.authn.mfa.simple.mail" %}

Moreover, you **MUST** define and implement the following `Bean` component in your CAS configuration that is
ultimately tasked to receive the user's registration details and update the relevant user record.

```java
@Bean
public CasSimpleMultifactorAuthenticationAccountService casSimpleMultifactorAuthenticationAccountService() {
    return new MySimpleMultifactorAuthenticationAccountService();
}
```

<div class="alert alert-info">:information_source: <strong>Remember</strong>
<p>Device registration flow is limited to registering email addresses. Support for registering
other forms of contact information may be worked out in future releases.</p>
</div>

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.

