---
layout: default
title: CAS - CAS WS Federation Protocol
---

# WS Federation Protocol


CAS can act as a standalone identity provider, presenting support for the [WS-Federation Passive Requestor Profile](http://docs.oasis-open.org/wsfed/federation/v1.2/os/ws-federation-1.2-spec-os.html#_Toc223175002). The core functionality
is built on top of [Apache Fediz](http://cxf.apache.org/fediz.html) whose architecture is described [here](http://cxf.apache.org/fediz-architecture.html).

## Security Token Service

The WS-Trust OASIS standard specifies a runtime component called Security Token Service. A service consumer requests a security token from the STS which is sent to the service provider. Either the service provider can validate the security token on its own or sends a request to the STS for validation. This pattern is based on an indirect trust relationship between the service provider and the STS instead of a direct trust between the service provider and service consumer. As long as the service consumer is in the possession of a security token issued by a trusted STS, the service provider accepts this security token.

A key benefit of the STS is the reduced complexity for applications. A web service consumer doesn't have to know how to create the various types of security tokens its service providers require. Instead, it sends a request to the STS containing the requirements of the client and the service provider and attaches the returned security token to the outgoing SOAP message to the service provider.
One service provider could require a SAML 1.1 token, another SAML 2.0 token and another custom binary security token. The service consumer doesn't have to understand SAML 1.1, SAML 2.0 or the custom binary security token. All he has to do is grab the returned token from the STS and attach it to the message. Thus, you can reduce the complexity in your application and move it to a centralized component.
A web service consumer requests tokens from an STS if the service provider defines an IssuedToken assertion in its security policy. This policy can contain some additional information like the address of the STS, token type, claims, etc.

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-ws-sts</artifactId>
  <version>${cas.version}</version>
</dependency>
```

<div class="alert alert-info"><strong>YAGNI</strong><p>You do not need to explicitly incude this component
in your configuration and overlays. The security token service will be pulled in automatically once you declare
the identity provider. Only include this module in your overlay if you need compile-time access to the components within.</p></div>

### Endpoints

| Endpoint               | Description
|------------------------|----------------------------------------------------------------------------------------------------------------------
| `/cas/ws/sts`          | Presents the list of available SOAP services and their WSDL configuration for each REALM defined in the configuration.


## WS Federation Identity Provider

The security model of the STS builds on the foundation established by WS-Security and WS-Trust. The primary issue for Web browsers is that there is no easy way to directly send web service (SOAP) requests. Consequently, the processing must be performed within the confines of the base HTTP 1.1 functionality (`GET`, `POST`, redirects, and cookies) and conform as closely as possible to the WS-Trust protocols for token acquisition.
The IDP is in charge of transforming the SignIn request of the browser to a SOAP request for the STS and the response of the STS to the SignInResponse for the browser. Further the browser user must authenticate himself with the IDP. At the time of initial authentication an artifact/cookie may be created for the browser so that every request for a resource doesn't require user interaction.

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-ws-idp</artifactId>
  <version>${cas.version}</version>
</dependency>
```

### Endpoints

| Endpoint                        | Description
|---------------------------------|--------------------------------------------------------------------------------------------------------
| `/cas/ws/idp/metadata`          | Displays the current federation metadata based on the configuration realm for the identity provider.

## Configuration

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html#ws-federation).

You may also need to declare the following Maven repository in
your CAS Overlay to be able to resolve dependencies:

```xml
<repositories>
    ...
    <repository>
        <id>shibboleth-releases</id>
        <url>https://build.shibboleth.net/nexus/content/repositories/releases</url>
    </repository>
    ...
</repositories>
```

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<AsyncLogger name="org.apache.cxf" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</AsyncLogger>
```
