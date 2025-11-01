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

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-ws-sts</artifactId>
  <version>${cas.version}</version>
</dependency>
```

<div class="alert alert-info"><strong>YAGNI</strong><p>You do not need to explicitly incude this component
in your configuration and overlays. This is just to teach you that it exists. The security token service will be pulled 
in automatically once you declare the identity provider. Only include this module in your overlay if you 
need compile-time access to the components within.</p></div>

### Endpoints

| Endpoint               | Description
|------------------------|----------------------------------------------------------------------------------------------------------------------
| `/cas/ws/sts`          | Presents the list of available SOAP services and their WSDL configuration for each REALM defined in the configuration.


### Security Tokens

Security tokens issued are treated as CAS tickets, stored in the ticket registry under 
the prefix `STS` and follow the same semantics as all other ticket types when it comes to persistence, 
replication, etc. These tokens are closely tied to the lifetime of the ticket-granting tickets and match 
their expiration policy. Tokens themselves do not have a lifespan outside a valid ticket-granting ticket 
and support for ticket lifetime configuration is not present.

## WS Federation Identity Provider

The security model of the STS builds on the foundation established by WS-Security and WS-Trust. 
The primary issue for Web browsers is that there is no easy way to directly send web service (SOAP) requests. 
Consequently, the processing must be performed within the confines of the base HTTP 1.1 functionality (`GET`, `POST`, redirects, and cookies) 
and conform as closely as possible to the WS-Trust protocols for token acquisition.
The IdP is in charge of transforming the sign-in request of the browser to a SOAP request for the STS and the response of the 
STS to the sign-in response for the browser. Further the browser user must authenticate with the IdP.

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
| `/cas/ws/idp/federation`        | Endpoint to receive initial `GET` authentication requests from clients, typically identified as the `issuer`.

## Realms

At this point, by default security token service's endpoint operate using a single realm configuration and identity provider 
configuration is only able to recognize and request tokens for a single realm.
While support for multiple realms is not there yet, in general the underlying configuration 
should allow for that feature to exist in later releases. The default realm recognized by 
CAS is set to be `urn:org:apereo:cas:ws:idp:realm-CAS`. Registration of clients need to ensure this value is matched.

## Register Clients

Clients and relying parties can be registered with CAS as such:

```json
{
  "@class" : "org.apereo.cas.ws.idp.services.WSFederationRegisteredService",
  "serviceId" : "https://wsfed.example.org/.+",
  "name" : "Sample WsFed Application",
  "id" : 100
}
```

| Field                         | Description
|-------------------------------|--------------------------------------------------------------------------------------------------
| `serviceId`                   | Callback/Consumer url where tokens may be `POST`ed, typically matching the `wreply` parameter.
| `realm`                       | The realm identifier of the application, identified via the `wtrealm` parameter. This needs to match the realm defined for the identity provider. By default it's set to the realm defined for the CAS identity provider.
| `appliesTo`                   | Controls to whom security tokens apply. Defaults to the `realm`.

Service definitions may be managed by the [service management](Service-Management.html) facility.

### Claims

The following claims are supported by CAS for release:

| Claim                           | Description
|---------------------------------|-----------------------------------------------------------------------------------------
| `EMAIL_ADDRESS_2005`            | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress`
| `EMAIL_ADDRESS`                 | `http://schemas.xmlsoap.org/claims/EmailAddress`
| `GIVEN_NAME`                    | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname`
| `NAME`                          | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name`
| `USER_PRINCIPAL_NAME_2005`      | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn`
| `USER_PRINCIPAL_NAME`           | `http://schemas.xmlsoap.org/claims/UPN`
| `COMMON_NAME`                   | `http://schemas.xmlsoap.org/claims/CommonName`
| `GROUP`                         | `http://schemas.xmlsoap.org/claims/Group`
| `MS_ROLE`                       | `http://schemas.microsoft.com/ws/2008/06/identity/claims/role`
| `ROLE`                          | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role`
| `SURNAME`                       | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname`
| `PRIVATE_ID`                    | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/privatepersonalidentifier`
| `NAME_IDENTIFIER`               | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier`
| `AUTHENTICATION_METHOD`         | `http://schemas.microsoft.com/ws/2008/06/identity/claims/authenticationmethod`
| `DENY_ONLY_GROUP_SID`           | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/denyonlysid`
| `DENY_ONLY_PRIMARY_SID`         | `http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarysid`
| `DENY_ONLY_PRIMARY_GROUP_SID`   | `http://schemas.microsoft.com/ws/2008/06/identity/claims/denyonlyprimarygroupsid`
| `GROUP_SID`                     | `http://schemas.microsoft.com/ws/2008/06/identity/claims/groupsid`
| `PRIMARY_GROUP_SID`             | `http://schemas.microsoft.com/ws/2008/06/identity/claims/primarygroupsid`
| `PRIMARY_SID`                   | `http://schemas.microsoft.com/ws/2008/06/identity/claims/primarysid`
| `WINDOWS_ACCOUNT_NAME`          | `http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname`
| `PUID`                          | `http://schemas.xmlsoap.org/claims/PUID`

The attribute release policy assigned to relying parties and services is able to link a given standard claim and map it to an attribute
that should be already available. The configuration looks as such:

```json
{
  "@class" : "org.apereo.cas.ws.idp.services.WSFederationRegisteredService",
  "serviceId" : "https://wsfed.example.org/.+",
  "realm" : "urn:wsfed:example:org:sampleapplication",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.ws.idp.services.WSFederationClaimsReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "GIVEN_NAME" : "givenName"
    }
  }
}
```

The above snippet allows CAS to release the claim `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname` whose value
is identified by the value of the `givenName` attribute that is already retrieved for the authenticated principal.

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
