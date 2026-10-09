---
layout: default
title: CAS - Passwordless Authentication - Passkeys
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - Passkeys

Passwordless authentication can offer passkeys next to its own token-based flow. Passkeys are handled by
[FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) acting as a primary authentication strategy, so the passkey
assertion is verified and the user is logged in exactly as with the passkey button of the CAS login form.

Passkeys are offered when WebAuthn is allowed to act as a primary authentication strategy:

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.core.allow-primary-authentication" %}

## Passkey Autofill

The passwordless username field is marked with `autocomplete="username webauthn"` and, where the browser supports
WebAuthn conditional mediation, CAS asks the browser for a passkey as soon as the page loads. The browser then lists the
passkeys it holds for CAS in the autofill menu of the username field, next to any saved usernames. Picking one completes
the login without a username or token; typing a username continues with the passwordless flow as usual. Browsers without
conditional mediation show the field as before.

## Selection Menu

When the [selection menu](Passwordless-Authentication-UserSelectionMenu.html) is available to the account, it
also offers a passkey option that asks the browser for a passkey with its own prompt.

## Requirements

- Passkeys must be registered as discoverable credentials. The passkey request is made before anyone is authenticated,
  so it names no credentials, and only passkeys that the authenticator can find on its own are offered.
  Registration takes place during [WebAuthn multifactor authentication](../mfa/FIDO2-WebAuthn-Authentication.html)
  or through the [device registration](../mfa/FIDO2-WebAuthn-Authentication-Registration.html) APIs.
- The passkey decides who logs in. A username typed on the passwordless page does not restrict which passkey may answer.
- Passkey providers that sync passkeys across devices, such as iCloud Keychain, Google Password Manager, 1Password or
  Bitwarden, usually register passkeys without attestation. Such registrations are rejected unless untrusted attestation is allowed via CAS settings.

<div class="alert alert-warning">:warning: <strong>User Verification</strong><p>
A passkey that logs the user in on its own should also verify the user with a PIN or biometric. Unless
<code class="cas-setting">cas.authn.mfa.web-authn.core.user-verification-requirement</code> is set to <code>REQUIRED</code>, the default
is <code>PREFERRED</code> and an authenticator that only checks for user presence, such as a security key without a PIN,
is accepted.</p></div>

<div class="alert alert-info">:information_source: <strong>Sessions</strong><p>
In browsers that support passkey autofill, every view of the passwordless username page starts a passkey request, which
creates an HTTP session on the server before the user does anything. Take this into account when sessions are shared
across CAS nodes.</p></div>
