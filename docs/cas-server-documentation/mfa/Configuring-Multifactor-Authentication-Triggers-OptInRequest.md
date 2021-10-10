---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Opt-In Request Parameter/Header - Multifactor Authentication Triggers

MFA can be triggered for a specific authentication request, provided
the initial request to the CAS `/login` endpoint contains a parameter/header
that indicates the required MFA authentication flow. The parameter/header name
is configurable, but its value must match the authentication provider id
of an available MFA provider described above.

{% include_cached casproperties.html properties="cas.authn.mfa.triggers.http" %}

An example request that triggers an authentication flow based on a request parameter would be:

```bash
https://.../cas/login?service=...&<PARAMETER_NAME>=<MFA_PROVIDER_ID>
```

The same strategy also applied to triggers that are based on request/session 
attributes, which tend to get used for internal communications between 
APIs and CAS components specially when designing extensions.
