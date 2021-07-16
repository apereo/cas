---
layout: default
title: CAS - Configuring Authentication Components
category: Authentication
---
{% include variables.html %}


# Configuration

The CAS authentication process is primarily controlled by an 
authentication manager, which orchestrates a collection of authentication handlers.

## Authentication Manager

CAS ships with a single yet flexible authentication manager which 
performs authentication according to the following contract.

For any given credential the manager does the following:

1. Iterate over all configured authentication handlers.
2. Attempt to authenticate a credential if a handler supports it.
3. On success attempt to resolve a principal.
  1. Check whether a resolver is configured for the handler that authenticated the credential.
  2. If a suitable resolver is found, attempt to resolve the principal.
  3. If a suitable resolver is not found, use the principal resolved by the authentication handler.
4. Check whether the security policy (e.g. any, all) is satisfied.
  1. If security policy is met return immediately.
  2. Continue if security policy is not met.
5. After all credentials have been attempted check security policy again and throw `AuthenticationException` if not satisfied.

There is an implicit security policy that requires at least one handler to successfully authenticate a credential.

## Authentication Handlers

There are a variety of authentication handlers and methods supported 
by CAS. Use the menu to navigate around the site and choose. By default, CAS is configured 
to accept a pre-defined set of credentials that are supplied via the CAS configuration.

<div class="alert alert-warning"><strong>Default Credentials</strong><p>To test the default authentication scheme in CAS,
use <strong>casuser</strong> and <strong>Mellon</strong> as the username and password respectively. These are automatically
configured via the static authentication handler, and <strong>MUST</strong> be removed from the configuration
prior to production rollouts.</p></div>

{% include casproperties.html properties="cas.authn.accept" %}

### Actuator Endpoints

The following endpoints are provided by CAS:

{% include actuators.html endpoints="authenticationHandlers" casModule="cas-server-support-reports" %}

### Resolution Strategy

Please see [this guide](Configuring-Authentication-Resolution.html) for more info.      

### Authentication Sequence

At runtime, CAS maintains a collection of authentication handlers/strategies that typically execute one after another. 
Each CAS module that presents a form of authentication strategy will insert itself into this collection at 
bootstrap time. At the end of this process, the result of all authentication transactions is collected and optionally processed by 
an authentication policy where success/failure of certain strategies/sources may be taken into account to fully satisfy the 
authentication requirements. The collection of authentication handlers tries to preserve order in a rather more deterministic way. 
The idea is that adopters can assign an `order` value to an authentication handler thereby explicitly positioning it in the 
collection and controlling its execution sequence.

### Authentication Pre/Post Processing

Please see [this guide](Configuring-Authentication-PrePostProcessing.html) for more details.           

### Naming Strategy

Each authentication handler in CAS can be named via CAS settings and if left undefined, the short name of 
the handler component itself is used (i.e. `LdapAuthenticationHandler`). The name itself can be any arbitrary string and typically is used
to identify and refer to the handler components in areas such as [required authentication for a service](../services/Configuring-Service-AuthN-Policy.html), etc.
In the event that multiple authentication handlers *of the same type* are defined, it is **RECOMMENDED** that each be given a unique name so as to avoid conflicts.
Authentication failures are typically collected in CAS by the name of each authentication handler. Leaving the name undefined will likely result in subsequent components in the authentication chain overriding previous results.

## Authentication Policy

Please see [authentication security policies](Configuring-Authentication-Policy.html) for more details.

## Principal Resolution

Please see [this guide](Configuring-Authentication-PrincipalResolution.html) for more details.

## Long Term Authentication

CAS has support for long term Ticket Granting Tickets, a feature that is also referred to as _"Remember Me"_
to extend the length of the SSO session beyond the typical configuration.
Please [see this guide](Configuring-LongTerm-Authentication.html) for more details.

## Proxy Authentication

Please [see this guide](Configuring-Proxy-Authentication.html) for more details.

## Multifactor Authentication (MFA)

Please [see this guide](../mfa/Configuring-Multifactor-Authentication.html) for more details.

## Login Throttling

CAS provides a facility for limiting failed login attempts to support password guessing and related abuse scenarios.
Please [see this guide](Configuring-Authentication-Throttling.html) for additional details on login throttling.

## SSO Session Cookie

A ticket-granting cookie is an HTTP cookie set by CAS upon the establishment of a single sign-on session.
This cookie maintains login state for the client, and while it is valid, the client can present it to CAS in lieu of primary credentials.
Please [see this guide](Configuring-SSO.html) for additional details.
