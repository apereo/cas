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

The following multifactor providers are supported by CAS.

| Provider              | Id              | Instructions
|-----------------------|-----------------|----------------------------------------------------------
| Duo Security          | `mfa-duo`       | [See this guide](DuoSecurity-Authentication.html).
| Authy Authenticator   | `mfa-authy`     | [See this guide](AuthyAuthenticator-Authentication.html).
| YubiKey               | `mfa-yubikey`   | [See this guide](YubiKey-Authentication.html).
| RSA/RADIUS            | `mfa-radius`    | [See this guide](RADIUS-Authentication.html).
| Google Authenticator  | `mfa-gauth`     | [See this guide](GoogleAuthenticator-Authentication.html).
| Microsoft Azure       | `mfa-azure`     | [See this guide](MicrosoftAzure-Authentication.html).
| FIDO U2F              | `mfa-u2f`       | [See this guide](FIDO-U2F-Authentication.html).


## Triggers

Multifactor authentication can be activated based on the following triggers.

### Global

MFA can be triggered for all applications and users regardless of individual settings.

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

### Global Principal Attribute

MFA can be triggered for all users/subjects carrying a specific attribute that matches one of the conditions below.

- Trigger MFA based on a principal attribute(s) whose value(s) matches a regex pattern.
**Note** that this behavior is only applicable if there is only a **single MFA provider** configured, since that would allow CAS
to know what provider to next activate.

- Trigger MFA based on a principal attribute(s) whose value(s) **EXACTLY** matches an MFA provider.
This option is more relevant if you have more than one provider configured or if you have the flexibility of assigning
provider ids to attributes as values.

Needless to say, the attributes need to have been resolved for the principal prior to this step.

### Global Authentication Attribute

MFA can be triggered for all users/subjects whose *authentication event/metadata* has resolved a specific attribute
that matches one of the below conditions:

- Trigger MFA based on a *authentication attribute(s)* whose value(s) matches a regex pattern.
**Note** that this behavior is only applicable if there is only a **single MFA provider** configured, since that would allow CAS
to know what provider to next activate.

- Trigger MFA based on a *authentication attribute(s)* whose value(s) **EXACTLY** matches an MFA provider.
This option is more relevant if you have more than one provider configured or if you have the flexibility of assigning
provider ids to attributes as values.

Needless to say, the attributes need to have been resolved for the authentication event prior to this step. This trigger
is generally useful when the underlying authentication engine signals CAS to perform additional validation of credentials.
This signal may be captured by CAS as an attribute that is part of the authentication event metadata which can then trigger
additional multifactor authentication events.

An example of this scenario would be the "Access Challenge response" produced by RADIUS servers.

### Adaptive

MFA can be triggered based on the specific nature of a request that may be considered outlawed. For instance,
you may want all requests that are submitted from a specific IP pattern, or from a particular geographical location
to be forced to go through MFA. CAS is able to adapt itself to various properties of the incoming request
and will route the flow to execute MFA. See [this guide](Configuring-Adaptive-Authentication.html) for more info.

### Grouper

MFA can be triggered by [Grouper](https://www.internet2.edu/products-services/trust-identity-middleware/grouper/)
groups to which the authenticated principal is assigned.
Groups are collected by CAS and then cross-checked against all available/configured MFA providers.
The group's comparing factor **MUST** be defined in CAS to activate this behavior
and it can be based on the group's name, display name, etc where
a successful match against a provider id shall activate the chosen MFA provider.

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-grouper</artifactId>
  <version>${cas.version}</version>
</dependency>
```

You will also need to ensure `grouper.client.properties` is available on the classpath
with the following configured properties:

```properties
grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
grouperClient.webService.login = banderson
grouperClient.webService.password = password
```

### REST

MFA can be triggered based on the results of a remote REST endpoint of your design. If the endpoint is configured,
CAS shall issue a `POST`, providing the principal id. The body of the response in the event of a successful `200`
status code is expected to be the MFA provider id which CAS should activate.

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
the authenticated principal carries an attribute that matches a configured attribute value. The attribute
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

## Bypass Rules

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

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#multifactor-authentication).

Note that ticket validation requests shall successfully go through if multifactor authentication is
bypassed for the given provider. In such cases, no authentication context is passed back to the application and
additional attributes are supplanted to let the application know multifactor authentication is bypassed for the provider.

### Applications

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

## Failure Modes

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
|----------------------|----------------------------------
| `CLOSED`                  | Authentication is blocked if the provider cannot be reached.
| `OPEN`                    | Authentication proceeds yet requested MFA is NOT communicated to the client if provider is unavailable.
| `PHANTOM`                 | Authentication proceeds and requested MFA is communicated to the client if provider is unavailable.
| `NONE`                    | Do not contact the provider at all to check for availability. Assume the provider is available.

A default failure mode can also be specified globally via CAS properties and may be overriden individually by CAS registered services.
To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#multifactor-authentication).

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

Ranking of authentication methods is done per provider via specific properties for each in CAS settings. Note that
the higher the rank value is, the higher on the security scale it remains. A provider that ranks higher with a larger weight value trumps
and override others with a lower value.

## Trusted Device/Browser

CAS is able to natively provide trusted device/browser features as part of any multifactor authentication flow. While certain providers
tend to support this feature as well, this behavior is now put into CAS directly providing you with exact control over how devices/browsers
are checked, how is that decision remembered for subsequent requests and how you might allow delegated management of those trusted decisions
both for admins and end-users.

[See this guide for more info](Multifactor-TrustedDevice-Authentication.html).

## Settings

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#multifactor-authentication).
