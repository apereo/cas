---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# FIDO2 WebAuthn Multifactor Authentication

[WebAuthn](https://webauthn.io/) is an API that makes it very easy 
for a relying party, such as a web service, to integrate strong 
authentication into applications using support built in to all leading browsers and platforms. This means 
that web services can now easily offer their users strong authentication with a choice of authenticators 
such as security keys or built-in platform authenticators such as biometric readers.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>To use WebAuth support in a cluster,
you must either enable session affinity (so that the same user always connects to the same node),
or share the web session across all nodes in the cluster.</p></div>

Support is enabled by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn" %}

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn" includes=".core,.crypto" excludes=".trust-source" %}

### Bypass

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn" includes=".bypass" %}

## Discoverable Credentials

It is possible to allow WebAuthN to act as a standalone authentication strategy for primary authentication. Using this approach,
user accounts and FIDO2-enabled devices that have already registered with 
CAS are given the option to login using their FIDO2-enabled device for a passwordless authentication experience.

> Discoverable Credential means that the private key and associated metadata is stored in persistent 
memory on the authenticator, instead of encrypted and stored on the relying party server. 

[Device registration](FIDO2-WebAuthn-Authentication-Registration.html) can occur out of band using 
available CAS APIs, or by allowing users to pass through the registration flow
as part of the typical multifactor authentication. See below for details on device registration.
