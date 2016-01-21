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

### DuoSecurity

Configure authentication per instructions [here] (DuoSecurity-Authentication.html). 

| Field                | Description
|----------------------+---------------------------------+
| `provider`           | `duoAuthenticationProvider`
| `id`                 | `mfa-duo`

### YubiKey

Configure authentication per instructions [here] (YubiKey-Authentication.html). 

| Field                | Description
|----------------------+---------------------------------+
| `provider`           | `yubikeyAuthenticationProvider`
| `id`                 | `mfa-yubikey`

### RSA/RADIUS

Configure authentication per instructions [here] (RADIUS-Authentication.html). 

| Field                | Description
|----------------------+---------------------------------+
| `provider`           | `radiusAuthenticationProvider`
| `id`                 | `mfa-radius`

## Triggers

Multifactor authentication can be activated based on the following triggers:

### Applications
MFA can be triggered for a specific application registered inside the CAS service registry.

{% highlight json %}

{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "authenticationPolicy" : {
    "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAuthenticationPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "duoAuthenticationProvider" ] ],
    "failOpen" : false
  }
}

{% endhighlight %}

### Principal Attribute
MFA can be triggered for all users/subjects carrying a specific attribute that matches configured attribute value. The attribute
value is a regex pattern and must match the authentication flow
id of an available MFA provider described above. See below to learn about how to configure MFA settings. 

### Opt-In Request Parameter
MFA can be triggered for a specific authentication request, provided
the initial request to the CAS `/login` endpoint contains a parameter
that indicates the required MFA authentication flow. The parameter name
is configurable, but its value must match the authentication provider id
of an available MFA provider described above. 

{% highlight bash %}

https://.../cas/login?service=...&<PARAMETER_NAME>=<MFA_PROVIDER_ID>

{% endhighlight %}

### Principal Attribute Per Application
As a hybrid option, MFA can be triggered for a specific application registered inside the CAS service registry, provided
the authenticated principal carries an attribute that matches configured attribute value. The attribute
value can be an arbitrary regex pattern. See below to learn about how to configure MFA settings.

{% highlight json %}

{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "authenticationPolicy" : {
    "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAuthenticationPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "duoAuthenticationProvider" ] ],
    "principalAttributeNameTrigger" : "memberOf",
    "principalAttributeValueToMatch" : "faculty|allMfaMembers"
  }
}

{% endhighlight %}

## Fail-Open vs Fail-Closed
The authentication policy by default supports fail-close mode, which means that if you attempt to exercise a particular
provider available to CAS and the provider is not available cannot be pinged, authentication will be stopped and an error
will be displayed. You can of course change this behavior so that authentication proceeds without exercising the provider
functionality, if that provider cannot respond. 

## Ranking Providers
At times, CAS needs to determine the correct provider when step-up authentication is required. Consider for a moment that CAS
already has established an SSO session with/without a provider and has established a level of authentication. Another incoming
request attempts to exercise that SSO session with a different and often competing authentication requirements that may differ
from the authentication level CAS has already established. Concretely, examples may be:

- CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity.
- CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity, but a separate request now requires step-up 
authentication with YubiKey. 

In certain scenarios, CAS will attempt to rank authentication levels and compare them with each other. If CAS already has achieved a level
that is higher than what the incoming request requires, no step-up authentication will be performed. If the opposite is true, CAS will
route the authentication flow to the required authentication level and upon success, will adjust the SSO session with the new higher 
authentication level now satisfied. 

Ranking of authentication methods is done per provider via specific properties for each in `cas.properties`.

## Settings
The following general MFA settings are available for configuration in `cas.properties`:

{% highlight properties %}
# cas.mfa.principal.attribute=
# cas.mfa.principal.attribute.value=
# cas.mfa.request.parameter=
{% endhighlight %}
