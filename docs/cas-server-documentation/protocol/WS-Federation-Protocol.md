---
layout: default
title: CAS - CAS WS Federation Protocol
category: Protocols
---

{% include variables.html %}

# WS Federation Protocol

CAS can act as a standalone identity provider, presenting support for 
the [WS-Federation Passive Requestor Profile](http://docs.oasis-open.org/wsfed/federation/v1.2/os/ws-federation-1.2-spec-os.html#_Toc223175002). 
The core functionality is built on top of [Apache Fediz](http://cxf.apache.org/fediz.html) 
whose architecture is described [here](http://cxf.apache.org/fediz-architecture.html).

<div class="alert alert-info"><strong>Remember</strong><p>The functionality described 
here allows CAS to act as an identity provider to support the WS-Federation protocol. If you wish to do the 
opposite and hand off authentication to an external identity provider that supports WS-Federation, you may take advantage of 
<a href="../integration/ADFS-Integration.html">Delegation</a> as one integration option.</p></div>

## Security Token Service

The WS-Trust OASIS standard specifies a runtime component called Security Token Service. A service 
consumer requests a security token from the STS which is sent to the service 
provider. Either the service provider can validate the security token on its 
own or sends a request to the STS for validation. This pattern is based on an 
indirect trust relationship between the service provider and the STS instead 
of a direct trust between the service provider and service consumer. As long 
as the service consumer is in the possession of a security token issued by 
a trusted STS, the service provider accepts this security token.

A key benefit of the STS is the reduced complexity for applications. A web 
service consumer does not have to know how to create the various types of security 
tokens its service providers require. Instead, it sends a request to the STS 
containing the requirements of the client and the service provider and 
attaches the returned security token to the outgoing SOAP message to the service provider.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ws-sts" %}

<div class="alert alert-info"><strong>YAGNI</strong><p>You do not need to explicitly include this component
in your configuration and overlays. This is just to teach you that it exists. The security token service will be pulled 
in automatically once you declare the identity provider. Only include this module in your overlay if you 
need compile-time access to the components within.</p></div>

### Endpoints

| Endpoint  | Description                                                                                                            |
|-----------|------------------------------------------------------------------------------------------------------------------------|
| `/ws/sts` | Presents the list of available SOAP services and their WSDL configuration for each REALM defined in the configuration. |

### Security Tokens

Security tokens issued are treated as CAS tickets, stored in the ticket registry under 
the prefix `STS` and follow the same semantics as all other ticket types when it comes to persistence, 
replication, etc. These tokens are closely tied to the lifetime of the ticket-granting tickets and match 
their expiration policy. Tokens themselves do not have a lifespan outside a valid ticket-granting ticket 
and support for ticket lifetime configuration is not present.

## WS Federation Identity Provider

The security model of the STS builds on the foundation established by WS-Security and WS-Trust. 
The primary issue for Web browsers is that there is no easy way to directly send web service (SOAP) requests. 
Consequently, the processing must be performed within the confines 
of the base HTTP 1.1 functionality (`GET`, `POST`, redirects, and cookies) 
and conform as closely as possible to the WS-Trust protocols for token acquisition.
The IdP is in charge of transforming the sign-in request of the 
browser to a SOAP request for the STS and the response of the 
STS to the sign-in response for the browser. Further the browser user must authenticate with the IdP.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ws-idp" %}

### Endpoints

| Endpoint             | Description                                                                                                   |
|----------------------|---------------------------------------------------------------------------------------------------------------|
| `/ws/idp/metadata`   | Displays the current federation metadata based on the configuration realm for the identity provider.          |
| `/ws/idp/federation` | Endpoint to receive initial `GET` authentication requests from clients, typically identified as the `issuer`. |

## Realms

At this point, by default security token service's endpoint operate 
using a single realm configuration and identity provider 
configuration is only able to recognize and request tokens for a single realm.
While support for multiple realms is not there yet, in general the underlying configuration 
should allow for that feature to exist in later releases. The default realm recognized by 
CAS is set to be `urn:org:apereo:cas:ws:idp:realm-CAS`. Registration of clients need to ensure this value is matched.

## Clients

Please see [this guide](WS-Federation-Protocol-Clients.html).

## Claims
                
Please see [this guide](WS-Federation-Protocol-Claims.html).

## Token Types

The following token types are supported by CAS:

| Type                                                                       |
|----------------------------------------------------------------------------|
| `http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1` |
| `http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0` |
| `urn:ietf:params:oauth:token-type:jwt`                                     |
| `http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512/sct`        |

Token type may be configured on a per-service basis:

```json
{
  "@class" : "org.apereo.cas.ws.idp.services.WSFederationRegisteredService",
  "serviceId" : "https://wsfed.example.org/.+",
  "realm" : "urn:wsfed:example:org:sampleapplication",
  "name" : "WSFED",
  "id" : 1,
  "tokenType": "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1"
}
```

## Configuration

{% include_cached casproperties.html modules="cas-server-support-ws-idp,cas-server-support-ws-sts" %}

You may also need to declare the following repository in
your CAS Overlay to be able to resolve dependencies:

```groovy
repositories {
    maven { 
        mavenContent { releasesOnly() }
        url "https://build.shibboleth.net/maven/releases/" 
    }
}
```

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.apache.cxf" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```
