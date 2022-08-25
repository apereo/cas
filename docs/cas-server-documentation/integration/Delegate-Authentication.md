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

<div class="alert alert-info"><strong>Note</strong><p>The client issuing the authentication request 
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

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.pac4j" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
