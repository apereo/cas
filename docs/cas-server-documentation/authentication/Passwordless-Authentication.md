---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication

Passwordless Authentication is a form of authentication in CAS where passwords take the 
form of tokens that expire after a configurable period of time. 
Using this strategy, users are asked for an identifier (i.e. username) which is used to locate the user record 
that contains forms of contact such as email and phone
number. Once located, the CAS-generated token is sent to the user via the configured notification 
strategies (i.e. email, sms, etc) where the user is then expected to 
provide the token back to CAS in order to proceed. 

<div class="alert alert-info">:information_source: <strong>No Magic Link</strong><p>
Presently, there is no support for magic links that would remove the task of providing the token 
back to CAS allowing the user to proceed automagically.
This variant may be worked out in future releases.</p></div>

In order to successfully implement this feature, configuration needs to be in place to contact 
account stores that hold user records who qualify for passwordless authentication. 
Similarly, CAS must be configured to manage issued tokens in order to execute find, 
validate, expire or save operations in appropriate data stores.

Qualifying passwordless accounts may also directly be routed to selected [multifactor authentication](Passwordless-Authentication-MFA.html) providers
or [delegated to external identity providers](Passwordless-Authentication-Delegation.html) for further verification. Alternatively,
the passwordless account may be instructed to allow the user to select from 
a [menu of available authentication options](Passwordless-Authentication-UserSelectionMenu.html).

## Passwordless Variants

Passwordless authentication can also be activated using [QR Code Authentication](QRCode-Authentication.html),
allowing end users to login by scanning a QR code using a mobile device.

Passwordless authentication can also be achieved via [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) which lets users 
verify their identities without passwords and login using FIDO2-enabled devices.

## Overview

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-passwordless-webflow" %}

{% include_cached casproperties.html properties="cas.authn.passwordless.core." %}

## Account Stores

Please see [this guide](Passwordless-Authentication-Account-Storage.html).

## Token Management

Please see [this guide](Passwordless-Authentication-Tokens.html).

### Messaging & Notifications

Please [see this](Passwordless-Authentication-Notifications.html) for details.

## Passwordless Authentication Per Application

Please see [this guide](Passwordless-Authentication-PerApplication.html) for details.

## reCAPTCHA Integration

Please see [this guide](Passwordless-Authentication-Recaptcha.html) for details.
