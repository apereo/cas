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

## Passwordless Variants

Passwordless authentication can also be activated using [QR Code Authentication](QRCode-Authentication.html),
allowing end users to login by scanning a QR code using a mobile device.

Passwordless authentication can also be 
achieved via [FIDO2 WebAuthn](../mfa/FIDO2-WebAuthn-Authentication.html) which lets users 
verify their identities without passwords and login using FIDO2-enabled devices.

## Overview

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-passwordless-webflow" %}

{% include_cached casproperties.html properties="cas.authn.passwordless.core." %}

## Account Stores

User records that qualify for passwordless authentication must 
be found by CAS using one of the following strategies. All strategies may be configured
using CAS settings and are activated depending on the presence of configuration values.

| Option  | Description                                                                |
|---------|----------------------------------------------------------------------------|
| Simple  | Please [see this guide](Passwordless-Authentication-Storage-Simple.html).  |
| MongoDb | Please [see this guide](Passwordless-Authentication-Storage-MongoDb.html). |
| LDAP    | Please [see this guide](Passwordless-Authentication-Storage-LDAP.html).    |
| JSON    | Please [see this guide](Passwordless-Authentication-Storage-JSON.html).    |
| Groovy  | Please [see this guide](Passwordless-Authentication-Storage-Groovy.html).  |
| REST    | Please [see this guide](Passwordless-Authentication-Storage-Rest.html).    |
| Custom  | Please [see this guide](Passwordless-Authentication-Storage-Custom.html).  |

## Token Management

The following strategies define how issued tokens may be managed by CAS. 

{% include_cached casproperties.html properties="cas.authn.passwordless.tokens" includes=".core,.crypto" %}

| Option  | Description                                                                                                     |
|---------|-----------------------------------------------------------------------------------------------------------------|
| Memory  | This is the default option where tokens are kept in memory using a cache with a configurable expiration period. |
| MongoDb | Please [see this guide](Passwordless-Authentication-Tokens-MongoDb.html).                                       |
| JPA     | Please [see this guide](Passwordless-Authentication-Tokens-JPA.html).                                           |
| REST    | Please [see this guide](Passwordless-Authentication-Tokens-Rest.html).                                          |
| Custom  | Please [see this guide](Passwordless-Authentication-Tokens-Custom.html).                                        |

### Messaging & Notifications

Please [see this](Passwordless-Authentication-Notifications.html) for details.

## Disabling Passwordless Authentication Flow

Passwordless authentication can be disabled conditionally on a per-user basis. If 
the passwordless account retrieved from the account store
carries a user whose `requestPassword` is set to `true`, the passwordless flow
(i.e. as described above with token generation, etc) will
be disabled and skipped in favor of the more usual CAS authentication flow, 
challenging the user for a password. Support for this behavior may depend
on each individual account store implementation.

## Multifactor Authentication

Please [see this](Passwordless-Authentication-MFA.html) for details.

## Delegated Authentication

Please [see this](Passwordless-Authentication-Delegation.html) for details.
