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

User records that qualify for passwordless authentication must 
be found by CAS using one of the following strategies. All strategies may be configured
using CAS settings and are activated depending on the presence of configuration values.

| Option       | Description                                                                    |
|--------------|--------------------------------------------------------------------------------|
| Simple       | Please [see this guide](Passwordless-Authentication-Storage-Simple.html).      |
| MongoDb      | Please [see this guide](Passwordless-Authentication-Storage-MongoDb.html).     |
| LDAP         | Please [see this guide](Passwordless-Authentication-Storage-LDAP.html).        |
| JSON         | Please [see this guide](Passwordless-Authentication-Storage-JSON.html).        |
| Groovy       | Please [see this guide](Passwordless-Authentication-Storage-Groovy.html).      |
| REST         | Please [see this guide](Passwordless-Authentication-Storage-Rest.html).        |
| Custom       | Please [see this guide](Passwordless-Authentication-Storage-Custom.html).      |
| Duo Security | Please [see this guide](Passwordless-Authentication-Storage-DuoSecurity.html). |
      
Note that Multiple passwordless account stores can be used simultaneously to verify and locate passwordless accounts.
 
### Account Customization

When a passwordless account is located from store, it may be customized and post-processed to modify
various aspects of the account such as the requirement to activate MFA, password flows, etc. CAS allows
for a Groovy script that is passed the retrieved passwordless account and script is responsible for adjustments
and modifications.

```groovy
import org.apereo.cas.api.*

def run(Object[] args) {
    def (account,applicationContext,logger) = args

    logger.info("Customizing $account")
    
    // Update the account...
    
    return account
}
```

The following parameters are passed to the script:

| Parameter            | Description                                                                  |
|----------------------|------------------------------------------------------------------------------|
| `account`            | The object representing the `PasswordlessUserAccount` that is to be updated. |
| `applicationContext` | The object representing the Spring application context.                      |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`.  |
                                                                              
Alternatively, you may build your own implementation of `PasswordlessUserAccountCustomizer` and register it as a Spring bean.

```java
@Bean
public PasswordlessUserAccountCustomizer myCustomizer() {
    return new MyPasswordlessUserAccountCustomizer();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.

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

## Passwordless Authentication Per Application

Passwordless authentication can be selectively controlled for specific applications. By default,
all services and applications are eligible for passwordless authentication.

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://app.example.org",
  "name": "App",
  "id": 1,
  "passwordlessPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServicePasswordlessPolicy",
    "enabled": false
  }
}
```

The following passwordless policy settings are supported:

| Name      | Description                                                                        |
|-----------|------------------------------------------------------------------------------------|
| `enabled` | Boolean to define whether passwordless authentication is allowed for this service. |


## reCAPTCHA Integration

Passwordless authentication attempts can be protected and integrated
with [Google reCAPTCHA](https://developers.google.com/recaptcha). This requires
the presence of reCAPTCHA settings for the basic integration and instructing
the password management flow to turn on and verify requests via reCAPTCHA.

{% include_cached casproperties.html properties="cas.authn.passwordless.google-recaptcha" %}
