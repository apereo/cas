---
layout: default
title: CAS - Multifactor Authentication Bypass
---

# Multifactor Authentication Bypass

Each multifactor provider is equipped with options to allow for MFA bypass. Once the provider
is chosen to honor the authentication request, bypass rules are then consulted to calculate
whether the provider should ignore the request and skip MFA conditionally.

Bypass rules allow for the following options for each provider:

- Skip multifactor authentication based on designated **principal** attribute **names**.
- ...[and optionally] Skip multifactor authentication based on designated **principal** attribute **values**.
- Skip multifactor authentication based on designated **authentication** attribute **names**.
- ...[and optionally] Skip multifactor authentication based on designated **authentication** attribute **values**.
- Skip multifactor authentication depending on method/form of primary authentication execution.

A few simple examples follow:

- Trigger MFA except when the principal carries an `affiliation` attribute whose value is either `alum` or `member`.
- Trigger MFA except when the principal carries a `superAdmin` attribute.
- Trigger MFA except if the method of primary authentication is SPNEGO.
- Trigger MFA except if credentials used for primary authentication are of type `org.example.MyCredential`.

Note that in addition to the above options, some multifactor authentication providers
may also skip and bypass the authentication request in the event that the authenticated principal does not quite "qualify"
for multifactor authentication. See the documentation for each specific provider to learn more.

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#multifactor-authentication).

Note that ticket validation requests shall successfully go through if multifactor authentication is
bypassed for the given provider. In such cases, no authentication context is passed back to the application and
additional attributes are supplanted to let the application know multifactor authentication is bypassed for the provider.

## Bypass Per Service

MFA Bypass rules can be overridden per application via the CAS service registry. This is useful when
MFA may be turned on globally for all applications and services, yet a few selectively need to be excluded. Services
whose access should bypass MFA may be defined as such in the CAS service registry:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ],
    "bypassEnabled" : "true"
  }
}
```
