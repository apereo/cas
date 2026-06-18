---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# Delegated Authentication

CAS can act as a client (i.e. service provider or proxy) using 
the [Pac4j library](https://github.com/pac4j/pac4j) and delegate the authentication to:

* CAS servers
* SAML2 identity providers
* OAuth2 providers such as Facebook, Twitter, GitHub, Google, LinkedIn, etc
* OpenID Connect identity providers such as Google, Apple
* [ADFS](ADFS-Integration.html)

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-webflow" %}

{% include_cached casproperties.html properties="cas.authn.pac4j.core" %}

<div class="alert alert-info">:information_source: <strong>Note</strong><p>The client issuing the authentication request 
can be of any type (SAML, OAuth2, OpenID Connect, etc) and is allowed to submit the 
authentication request using any protocol that the CAS server supports and is configured 
to understand. This means that you may have an OAuth2 client using CAS in delegation 
mode to authenticate at an external SAML2 identity provider, another CAS server or 
Facebook and in the end of that flow receiving an OAuth2 user profile. The CAS 
server is able to act as a proxy, doing the protocol translation in the middle.</p></div>

## Identity Provider Registration

Please [see this guide](Delegate-Authentication-Provider-Registration.html).

## Profile Attributes

In CAS-protected applications, through service ticket validation, user information
are pushed to the CAS client and therefore to the application itself.

The identifier of the user is always pushed to the CAS client. For user attributes, it involves both the configuration
at the server and the way of validating service tickets.

On CAS server side, to push attributes to the CAS client, it should be configured in the expected service:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "name", "first_name", "middle_name" ] ]
  }
}
```

## Discovery Selection

Please [see this guide](Delegate-Authentication-DiscoverySelection.html).

## Authentication Policy

Please [see this guide](Delegate-Authentication-AuthenticationPolicy.html).

## Provisioning

Please [see this guide](Delegate-Authentication-Provisioning.html).

## Post Processing

Please [see this guide](Delegate-Authentication-PostProcessing.html).

## Webflow

Certain aspects of the webflow configuration for delegated authentication can be controlled
via the following settings:

{% include_cached casproperties.html properties="cas.authn.pac4j.webflow" %}

## Multitenancy

Configuration settings for delegated authentication to any and all identity providers can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.
   
## Impersonation

Accounts that are successfully authenticated via delegated authentication may be allowed
to go through the impersonation flow. This behavior needs to be explicitly enabled in CAS configuration settings.

Please [see this guide](../authentication/Surrogate-Authentication.html) for more information.
 
## Multifactor Authentication

CAS can recognize when multifactor authentication has been satisfied by an upstream delegated identity provider, such as Microsoft Entra ID. 
The identity provider is expected to return a response to CAS that includes authentication method information and context as attributes  
indicating that multiple authentication methods were used. CAS inspects the delegated authentication result during 
the authentication post-processing phase and runs the following checks:

- Checks for the presence of a specific attribute matching an expected value.
- If a match is found, CAS treats the authentication event as having already satisfied MFA.
- CAS evaluates multifactor authentication triggers to see which may result into a multifactor provider for the effective user.
- List of matching multifactor providers are recorded into the CAS authentication as the effective authentication context.
      
The specific attributes that are examined to kickstart the process are the following:

| Attribute                                                    | Value                                               |
|--------------------------------------------------------------|-----------------------------------------------------|
| `http://schemas.microsoft.com/claims/authnmethodsreferences` | `http://schemas.microsoft.com/claims/multipleauthn` |
| `amr`                                                        | Any one of `mfa`, `hwk`, `swk`, `phr`, `phrh`       |

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Remember that the above list of attributes and values are just a starting point, and the list will grow over time.</p></div>

A sample authentication flow works as follows:

1. The user attempts to access an application that is protected by CAS and configured to delegate authentication to SAML2 identity provider such as Microsoft Entra ID.
2. CAS receives the request and redirects the user to SAML2 identity provider using the configured SAML2 integration.
3. SAML2 identity provider authenticates the user and enforces MFA using its own machinery.
4. After the user successfully completes MFA, SAML2 identity provider (i.e. Entra ID) sends a SAML response back to CAS.
5. The SAML2 response includes the authentication methods reference attribute:

```
http://schemas.microsoft.com/claims/authnmethodsreferences
```

...with the value:

```
http://schemas.microsoft.com/claims/multipleauthn
```

6. CAS receives the delegated authentication result and verifies the presence of above attributes.
7. CAS runs a selection strategy to determine a matching multifactor provider for the user.
8. CAS records the `authnContextClass` into its own authentication transaction with the values of the resolver multifactor provider ids (i.e. `[mfa-duo]`)
9. CAS creates the user’s SSO session and records that MFA has already been satisfied for this authentication event.
10. The user later accesses another CAS-protected application that normally requires MFA, **directly handled in CAS**.
11. CAS evaluates the existing SSO session, detects the recorded MFA context, and determines that the user has already satisfied the MFA requirement.
12. CAS grants access to the second application without prompting the user for MFA again.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.pac4j" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
