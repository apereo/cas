---
layout: default
title: CAS - Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication (MFA)

CAS provides support for a variety of multifactor authentication providers and 
options, while allowing one to design their own. The secondary authentication 
factor always kicks in *after* the primary step and existing authentication 
sessions will be asked to step-up to the needed multifactor authentication 
factor, should the request or trigger require it. The satisfied authentication 
context is communicated back to the application as well to denote a successful 
multifactor authentication event.

At a minimum, you need answer the following questions:

- Which provider(s) are we using for multifactor authentication?
- How and for whom are we triggering multifactor authentication?

## Supported Providers

The following multifactor providers are supported by CAS.

| Provider             | Id             | Instructions                                               |
|----------------------|----------------|------------------------------------------------------------|
| Duo Security         | `mfa-duo`      | [See this guide](DuoSecurity-Authentication.html).         |
| YubiKey              | `mfa-yubikey`  | [See this guide](YubiKey-Authentication.html).             |
| RSA/RADIUS           | `mfa-radius`   | [See this guide](RADIUS-Authentication.html).              |
| Google Authenticator | `mfa-gauth`    | [See this guide](GoogleAuthenticator-Authentication.html). |
| FIDO2 WebAuthN       | `mfa-webauthn` | [See this guide](FIDO2-WebAuthn-Authentication.html).      |
| CAS Simple           | `mfa-simple`   | [See this guide](Simple-Multifactor-Authentication.html).  |
| Twilio               | `mfa-twilio`   | [See this guide](Twilio-Multifactor-Authentication.html).  |
| Inwebo               | `mfa-inwebo`   | [See this guide](Inwebo-Authentication.html).              |
| Custom               | Custom         | [See this guide](Custom-MFA-Authentication.html).          |

<div class="alert alert-info">:information_source: <strong>Azure Multifactor</strong>
<p>Microsoft has removed the ability for external SSO servers and identity providers to use Azure MFA
as a standalone MFA solution. To use Azure MFA, you must also have all your users authenticate using Azure AD SSO.
You may want to route authentication requests to Azure AD SSO using the delegated authentication features of CAS.</p></div>

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
Support for Google Authenticator is not about supporting the mobile application itself. 
This is about supporting the TOTP algorithm that is used by the application to generate
time-based one-time passwords. Any compliant mobile application that can generate TOTP codes
can be used with CAS including LastPass, Microsoft Authenticator, Authy, etc.
</p></div>

## Configuration

{% include_cached casproperties.html properties="cas.authn.mfa.core" %}

## Triggers

Multifactor authentication can be activated via a number of triggers.
To learn more, [please see this guide](Configuring-Multifactor-Authentication-Triggers.html).

## Bypass Rules

Each multifactor provider is equipped with options to allow for MFA 
bypass. To learn more, [please see this guide](../mfa/Configuring-Multifactor-Authentication-Bypass.html).

## Failure Modes

CAS will consult the current configuration in the event that the provider being requested is 
unreachable to determine how to proceed. To learn more, [please see this guide](../mfa/Configuring-Multifactor-Authentication-FailureModes.html).

## Provider Selection

There are options and controls available to allow CAS to select a multifactor authentication provider, in case multiple triggers and conditions
activate multiple providers. To learn more, [please see this guide](Multifactor-Authentication-ProviderSelection.html).
