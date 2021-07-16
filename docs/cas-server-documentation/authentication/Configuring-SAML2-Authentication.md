---
layout: default
title: CAS - SAML2 Authentication
category: Protocols
---
{% include variables.html %}

# SAML2 Authentication

CAS can act as a SAML2 identity provider accepting authentication requests and producing SAML assertions.

If you intend to allow CAS to delegate authentication to an external SAML2 identity provider, you need to [review this guide](../integration/Delegate-Authentication.html).

<div class="alert alert-info"><strong>SAML Specification</strong><p>This document solely focuses on what one might do to turn on SAML2 
support inside CAS. It is not to describe/explain the numerous characteristics of the SAML2 protocol itself. If you are unsure 
about the concepts referred to on this page, please start with reviewing 
the <a href="http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html">SAML2 Specification</a>.</p></div>

## Federation Interop Evaluation

The CAS project strives to conform to the [SAML V2.0 Implementation Profile for Federation Interoperability](https://kantarainitiative.github.io/SAMLprofiles/fedinterop.html). An 
evaluation of the requirements against the current CAS release is available [here](https://docs.google.com/spreadsheets/d/1NYN5n6AaNxz0UxwkzIDuXMYL1JUKNZZlSzLZEDUw4Aw/edit?usp=sharing). It 
is recommended that you view, evaluate and comment on functionality that is currently either absent or marked questionable where verification is needed.

## SAML Endpoints

The following CAS endpoints respond to supported SAML2 profiles:

- `/idp/profile/SAML2/Redirect/SSO`
- `/idp/profile/SAML2/POST/SSO`
- `/idp/profile/SAML2/POST-SimpleSign/SSO`
- `/idp/profile/SAML2/POST/SLO`
- `/idp/profile/SAML2/Redirect/SLO`
- `/idp/profile/SAML2/Unsolicited/SSO`
- `/idp/profile/SAML2/SOAP/ECP`
- `/idp/profile/SAML2/SOAP/AttributeQuery`
- `/idp/profile/SAML1/SOAP/ArtifactResolution`

## Metadata Management

Handling and storing SAML2 identity provider or service provider metadata 
can be done in a few ways. To learn more, please [review this guide](../installation/Configuring-SAML2-DynamicMetadata.html).

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp" %}

You may also need to declare the following repository in
your CAS overlay to be able to resolve dependencies:

```groovy
repositories {
    maven { 
        mavenContent { releasesOnly() }
        url "https://build.shibboleth.net/nexus/content/repositories/releases" 
    }
}
```

{% include casproperties.html properties="cas.authn.saml-idp.core,cas.client,cas.session-replication" %}

### Actuator Endpoints

The following endpoints are provided by CAS:

{% include actuators.html endpoints="samlPostProfileResponse" %}

### SAML Services

Please [see this guide](../services/SAML2-Service-Management.html) to learn more 
about how to configure SAML2 service providers.

### Security Configuration

Please [see this guide](../installation/Configuring-SAML2-Security.html) to learn more
about how to configure SAML2 security configuration.

### Attribute Release

Attribute filtering and release policies are defined per SAML service. See [this guide](../installation/Configuring-SAML2-Attribute-Release.html) for more info.

### Name ID Selection

Please [see this guide](../installation/Configuring-SAML2-NameID.html) to learn more
about how to configure SAML2 security configuration.
  
## Unsolicited SSO

SAML2 IdP `Unsolicited/SSO` profile supports the following parameters:

| Parameter                         | Description
|-----------------------------------|-----------------------------------------------------------------
| `providerId`                      | Required. Entity ID of the service provider.
| `shire`                           | Optional. Response location (ACS URL) of the service provider.
| `target`                          | Optional. Relay state.
| `time`                            | Optional. Skew the authentication request.

## Attribute Queries

In order to allow CAS to support and respond to attribute queries, you need to make sure the generated metadata has
the `AttributeAuthorityDescriptor` element enabled, with protocol support enabled for `urn:oasis:names:tc:SAML:2.0:protocol`
and relevant binding that corresponds to the CAS endpoint(s). You also must ensure the `AttributeAuthorityDescriptor` tag lists all
`KeyDescriptor` elements and certificates that are used for signing as well as authentication, specially if the SOAP client of the service provider 
needs to cross-compare the certificate behind the CAS endpoint with what is defined for the `AttributeAuthorityDescriptor`. CAS by default 
will always use its own signing certificate for signing of the responses generated as a result of an attribute query.

Also note that support for attribute queries need to be explicitly enabled and the behavior is off by default, given it imposes a burden on 
CAS and the underlying ticket registry to keep track of attributes and responses as tickets and have them be later used and looked up.

## Client Libraries

For Java-based applications, the following frameworks may be used to integrate your application with CAS acting as a SAML2 identity provider:

- [Spring Security SAML](http://projects.spring.io/spring-security-saml/)
- [Pac4j](http://www.pac4j.org/docs/clients/saml.html)

## Sample Client Applications

- [Spring Security SAML Sample Webapp](https://github.com/apereo/saml2-sample-java-webapp)
- [Okta](https://developer.okta.com/standards/SAML/setting_up_a_saml_application_in_okta)

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.opensaml" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```
