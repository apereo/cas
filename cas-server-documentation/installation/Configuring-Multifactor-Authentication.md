---
layout: default
title: CAS - Multifactor Authentication
---

# Multifactor Authentication (MFA)

CAS provides a framework for multifactor authentication (MFA). The design philosophy for MFA support follows from
the observation that institutional security policies with respect to MFA vary dramatically. We provide first class
API support for authenticating multiple credentials and a policy framework around authentication. The components
could be extended in a straightforward fashion to provide higher-level behaviors such as Webflow logic to assist,
for example, a credential upgrade scenario where a SSO session is started by a weaker credential but a particular
service demands re-authentication with a stronger credential.

The authentication subsystem in CAS natively supports handling multiple credentials. While the default login form
and Webflow tier are designed for the simple case of accepting a single credential, all core API components that
interface with the authentication subsystem accept one or more credentials to authenticate.

## Supported Providers

### Duo Security

Configure authentication per instructions [here](DuoSecurity-Authentication.html). 

| Field                | Description
|----------------------+---------------------------------+
| `id`                 | `mfa-duo`

### YubiKey

Configure authentication per instructions [here](YubiKey-Authentication.html). 

| Field                | Description
|----------------------+---------------------------------+
| `id`                 | `mfa-yubikey`

### RSA/RADIUS

Configure authentication per instructions [here](RADIUS-Authentication.html). 

| Field                | Description
|----------------------+---------------------------------+
| `id`                 | `mfa-radius`

### Google Authenticator

Configure authentication per instructions [here](GoogleAuthenticator-Authentication.html). 

| Field                | Description
|----------------------+---------------------------------+
| `id`                 | `mfa-gauth`

## Triggers

Multifactor authentication can be activated based on the following triggers:

### Applications
MFA can be triggered for a specific application registered inside the CAS service registry.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ]
  }
}
```

### Principal Attribute
MFA can be triggered for all users/subjects carrying a specific attribute that matches configured attribute value. The attribute
value is a regex pattern and must match the provider
id of an available MFA provider described above. See below to learn about how to configure MFA settings. 

### Opt-In Request Parameter
MFA can be triggered for a specific authentication request, provided
the initial request to the CAS `/login` endpoint contains a parameter
that indicates the required MFA authentication flow. The parameter name
is configurable, but its value must match the authentication provider id
of an available MFA provider described above. 

```bash
https://.../cas/login?service=...&<PARAMETER_NAME>=<MFA_PROVIDER_ID>
```

### Principal Attribute Per Application
As a hybrid option, MFA can be triggered for a specific application registered inside the CAS service registry, provided
the authenticated principal carries an attribute that matches configured attribute value. The attribute
value can be an arbitrary regex pattern. See below to learn about how to configure MFA settings.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ],
    "principalAttributeNameTrigger" : "memberOf",
    "principalAttributeValueToMatch" : "faculty|allMfaMembers"
  }
}
```

## Fail-Open vs Fail-Closed
The authentication policy by default supports fail-closed mode, which means that if you attempt to exercise a particular
provider available to CAS and the provider cannot be reached, authentication will be stopped and an error
will be displayed. You can of course change this behavior so that authentication proceeds without exercising the provider
functionality, if that provider cannot respond. 

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ],
    "failureMode" : "CLOSED"
  }
}
```

The following failure modes are supported:

| Field                | Description
|----------------------+---------------------------------+
| `CLOSED`                  | Authentication is blocked if the provider cannot be reached. 
| `OPEN`                    | Authentication proceeds yet requested MFA is NOT communicated to the client if provider is unavailable.
| `PHANTOM`                 | Authentication proceeds and requested MFA is communicated to the client if provider is unavailable.
| `NONE`                    | Do not contact the provider at all to check for availability. Assume the provider is available.

## Ranking Providers
At times, CAS needs to determine the correct provider when step-up authentication is required. Consider for a moment that CAS
already has established an SSO session with/without a provider and has reached a level of authentication. Another incoming
request attempts to exercise that SSO session with a different and often competing authentication requirement that may differ
from the authentication level CAS has already established. Concretely, examples may be:

- CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity.
- CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity, but a separate request now requires step-up 
authentication with YubiKey. 

In certain scenarios, CAS will attempt to rank authentication levels and compare them with each other. If CAS already has achieved a level
that is higher than what the incoming request requires, no step-up authentication will be performed. If the opposite is true, CAS will
route the authentication flow to the required authentication level and upon success, will adjust the SSO session with the new higher 
authentication level now satisfied. 

Ranking of authentication methods is done per provider via specific properties for each in `application.properties`. Note that
the higher the rank value is, the higher on the security scale it remains. A provider that ranks higher with a larger weight value trumps 
and override others with a lower value. 

## Settings

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).
