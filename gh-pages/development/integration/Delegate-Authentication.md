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

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-webflow" %}

{% include casproperties.html properties="cas.authn.pac4j.core" %}

<div class="alert alert-info"><strong>Note</strong><p>The client issuing the authentication request 
can be of any type (SAML, OAuth2, OpenID Connect, etc) and is allowed to submit the 
authentication request using any protocol that the CAS server supports and is configured 
to understand. This means that you may have an OAuth2 client using CAS in delegation 
mode to authenticate at an external SAML2 identity provider, another CAS server or 
Facebook and in the end of that flow receiving an OAuth2 user profile. The CAS 
server is able to act as a proxy, doing the protocol translation in the middle.</p></div>

## Register Providers

An identity provider is a server which can authenticate users (like Google, Yahoo...) instead of a CAS server.
If you want to delegate the CAS authentication to Twitter for example, you have to add an
OAuth client for the Twitter provider, which will be done automatically for you once provider settings are taught to CAS.

Notice that for each OAuth provider, the CAS server is considered as an OAuth client and therefore should be declared as
an OAuth client at the OAuth provider. After the declaration, a key and a secret is given by the OAuth provider which has
to be defined in the CAS configuration as well.

### Default

{% assign providers = "DropBox,Facebook,FourSquare,Google,HiOrgServer,Orcid,PayPal,Twitter,WindowsLive,Wordpress,Yahoo,CAS,LinkedIn,GitHub,OAuth20,Google-OpenID-Connect,Keycloak,Azure-AD,Apple,Generic-OpenID-Connect" | split: "," | sort %}

Identity providers for delegated authentication can be registered with CAS using settings. 

<table>
  <thead>
    <tr><th>Provider</th><th>Reference</th></tr>
  </thead>
  <tbody>
    {% for provider in providers %}
    <tr>
    <td>{{ provider | replace: "-", " " }} </td>
    <td><a href="Delegate-Authentication-{{ provider }}.html">See this guide</a>.</td>
    </tr>
    {% endfor %}
  </tbody>
</table>

### REST

Identity providers for delegated authentication can be provided to CAS 
using an external REST endpoint. 

{% include casproperties.html properties="cas.authn.pac4j.rest" %}

This allows the CAS server to reach to 
a remote REST endpoint whose responsibility is to produce the following payload in the response body:

```json
{
    "callbackUrl": "https://sso.example.org/cas/login",
    "properties": {
        "github.id": "...",
        "github.secret": "...",
        
        "cas.loginUrl.1": "...",
        "cas.protocol.1": "..."
    }
}
```

The syntax and collection of available `properties` in the above 
payload is controlled by [Pac4j]((https://pac4j.org/docs/index.html). 
The response that is returned must be accompanied by a 200 status code.

{% include casproperties.html properties="cas.authn.pac4j.rest" %}

## Profile Attributes

In CAS-protected applications, through service ticket validation, user information
are pushed to the CAS client and therefore to the application itself.

The identifier of the user is always pushed to the CAS client. For user attributes, it involves both the configuration
at the server and the way of validating service tickets.

On CAS server side, to push attributes to the CAS client, it should be configured in the expected service:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
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

## Access Strategy

Service definitions may be conditionally authorized to use an external identity provider by defining their own access strategy and policy:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "delegatedAuthenticationPolicy" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
      "allowedProviders" : [ "java.util.ArrayList", [ "Facebook", "Twitter" ] ],
      "permitUndefined": true,
      "exclusive": true
    }
  }
}
```

Note that:

- The list of allowed providers should contain the external identity provider names (i.e. client names).
- The `permitUndefined` flag decides whether access should be granted in the event that no allowed providers are defined explicitly.
- The `exclusive` flag decides whether authentication should be exclusively limited to allowed providers, disabling other methods such as username/password, etc.

## Provisioning

Please [see this guide](Delegate-Authentication-Provisioning.html).

## SAML2 Identity Providers

To learn more about delegating authentication to SAML2 identity providers, 
please [review this guide](Delegate-Authentication-SAML.html).
 
## Session Replication
                
For the current active session, the selected identity provider, the relying party
and all other relevant details for the given authentication request are tracked as 
*session attributes* inside a dedicated session store capable of replication, which is specially
more relevant for clustred deployments.

{% include casproperties.html properties="cas.session-replication" %}
 
## Identity Provider Selection

The selected identity provider can be optionally tracked and stored using a dedicated cookie,
which will then be used on subsequent attempts to auto-redirect to 
the identity provider, skipping the selection menu.

{% include casproperties.html properties="cas.authn.pac4j.cookie" %}

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
