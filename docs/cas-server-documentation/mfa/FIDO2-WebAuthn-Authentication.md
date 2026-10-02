---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# FIDO2 WebAuthn (Passkey) Multifactor Authentication

[WebAuthn](https://webauthn.io/) is an API that makes it very easy 
for a relying party, such as a web service, to integrate strong 
authentication into applications using support built in to all leading browsers and platforms. This means 
that web services can now easily offer their users strong authentication with a choice of authenticators 
such as security keys or built-in platform authenticators such as biometric readers.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>To use WebAuthn support in a cluster,
you must either enable session affinity (so that the same user always connects to the same node),
or <a href="../webflow/Webflow-Customization-Sessions-ServerSide.html">replicate the web session</a> 
across all nodes in the cluster.</p></div>

Support is enabled by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn" %}

<div class="alert alert-info">:information_source: <strong>WebAuthn vs Passkeys</strong><p>
<strong>WebAuthn (Web Authentication)</strong> is a W3C specification and browser API 
that enables web applications to register 
and authenticate users using public-key cryptography in a phishing-resistant way.
<strong>Passkeys</strong> are a specific type of WebAuthn credential designed to replace 
passwords by using asymmetric key pairs. During registration, an authenticator on 
the user’s device generates a private-public key pair; the public key is sent to the 
service, and the private key remains securely on the device (or synced via a cloud backup).
In summary, WebAuthn is the underlying protocol/API that supports multiple authentication 
methods (hardware keys, platform authenticators, etc.), whereas passkeys are a user-facing 
credential format specifically built on WebAuthn for passwordless login.
</p></div>

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
as part of the typical multifactor authentication. 

The same passkeys are also offered by [passwordless authentication](../authentication/Passwordless-Authentication-Passkeys.html),
from the autofill menu of its username field and from its selection menu.

## Related Origins

A passkey is bound to the relying party identifier (`cas.authn.mfa.web-authn.core.relying-party-id`, or the host of the
CAS server name). When CAS is reached from origins whose domain differs from that identifier, list them under
`cas.authn.mfa.web-authn.core.allowed-origins`. CAS accepts assertions from those origins and publishes them for
[WebAuthn related origin requests](https://www.w3.org/TR/webauthn-3/#sctn-related-origins) at `/.well-known/webauthn`:

```json
{
  "origins": [
    "https://sso.example.org",
    "https://login.example.co.uk"
  ]
}
```

Browsers fetch this document from `https://<relying party identifier>/.well-known/webauthn`, at the root of the host
and outside the CAS context path, and they only honor a limited number of distinct registrable domains in it. When CAS
runs under a context path such as `/cas`, route the document onto the path CAS serves, either in the proxy that fronts
CAS or with the [embedded Tomcat rewrite valve](../installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html)
registered on the engine:

```
RewriteRule ^/\.well-known/webauthn$ /cas/.well-known/webauthn [L]
```

## Passkey Endpoints

CAS publishes the [passkey endpoints metadata](https://www.w3.org/TR/passkey-endpoints/) at
`/.well-known/passkey-endpoints`, which password managers and passkey providers read to send users to the pages
where passkeys are created (`enroll`) and managed (`manage`):

```json
{
  "enroll": "https://sso.example.org/cas/account",
  "manage": "https://sso.example.org/cas/account"
}
```

Each URL is taken from CAS settings. When one is not set and [account management](../registration/Account-Management-Overview.html) is enabled, it points to the
account profile, where WebAuthn devices are listed and registered; otherwise it is left out,
and an empty document still tells clients that CAS supports passkeys. Like the related origins document, clients fetch it from
the root of the relying party identifier's host, so route it onto the CAS context path the same way:

```
RewriteRule ^/\.well-known/passkey-endpoints$ /cas/.well-known/passkey-endpoints [L]
```

## Signal API

After a successful WebAuthn authentication, CAS uses the [WebAuthn Signal API](https://www.w3.org/TR/webauthn-3/#sctn-signal-methods),
where the browser supports it, to report the passkeys it still accepts for the user and the user's current name and
display name. Password managers and platform authenticators can then stop offering passkeys that were removed from
CAS and show the account as it is named in CAS. Browsers without the Signal API ignore this.

## Passkey Provider Names

Each registration keeps the AAGUID that identifies the authenticator or passkey provider that created it. When the
attestation does not name the device, which is the case for synced passkeys, the
[account profile](../registration/Account-Management-Overview.html) shows the provider name, such as
*Google Password Manager*, *1Password* or *Apple Passwords*, from a bundled snapshot of the community
[passkey provider AAGUID list](https://github.com/passkeydeveloper/passkey-authenticator-aaguids). The registration
page shows the same name, with the provider's icon from that list, right after a passkey is registered. Providers that
send an all-zero AAGUID, and devices registered before CAS kept the AAGUID, stay unnamed.
