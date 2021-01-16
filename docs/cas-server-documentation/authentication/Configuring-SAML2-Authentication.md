---
layout: default
title: CAS - SAML2 Authentication
category: Protocols
---
{% include variables.html %}

# SAML2 Authentication

CAS can act as a SAML2 identity provider accepting authentication requests and producing SAML assertions.

If you intend to allow CAS to delegate authentication to an external SAML2 identity provider, you need to [review this guide](../integration/Delegate-Authentication.html).

<div class="alert alert-info"><strong>SAML Specification</strong><p>This document solely focuses on what one might do to turn on SAML2 support inside CAS. It is not to describe/explain the numerous characteristics of the SAML2 protocol itself. If you are unsure about the concepts referred to on this page, please start with reviewing the <a href="http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html">SAML2 Specification</a>.</p></div>

## Federation Interop Evaluation

The CAS project strives to conform to the [SAML V2.0 Implementation Profile for Federation Interoperability](https://kantarainitiative.github.io/SAMLprofiles/fedinterop.html). An evaluation of the requirements against the current CAS release is available [here](https://docs.google.com/spreadsheets/d/1NYN5n6AaNxz0UxwkzIDuXMYL1JUKNZZlSzLZEDUw4Aw/edit?usp=sharing). It is recommended that you view, evaluate and comment on functionality that is currently either absent or marked questionable where verification is needed.

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

## IdP Metadata

The following CAS endpoints handle the generation of SAML2 metadata:

- `/idp/metadata`

This endpoint will display the CAS IdP SAML2 metadata upon receiving a GET request. If metadata is already available and generated,
it will be displayed. If metadata is absent, one will be generated automatically.
CAS configuration below dictates where metadata files/keys will be generated and stored.
  
{% include {{ version }}/saml2-idp-metadata-configuration.md %}

Note that the endpoint can accept a `service` parameter either by entity id or numeric identifier. This parameter
is matched against the CAS service registry allowing the endpoint to calculate and combine any identity provider 
metadata overrides that may have been specified.

Here is a generated metadata file as an example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<EntityDescriptor xmlns="urn:oasis:names:tc:SAML:2.0:metadata" xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                xmlns:shibmd="urn:mace:shibboleth:metadata:1.0" xmlns:xml="http://www.w3.org/XML/1998/namespace"
                xmlns:mdui="urn:oasis:names:tc:SAML:metadata:ui" entityID="ENTITY_ID">
    <IDPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
        <Extensions>
            <shibmd:Scope regexp="false">SCOPE</shibmd:Scope>
        </Extensions>
        <KeyDescriptor use="signing">
            <ds:KeyInfo>
              <ds:X509Data>
                  <ds:X509Certificate>...</ds:X509Certificate>
              </ds:X509Data>
            </ds:KeyInfo>
        </KeyDescriptor>
        <KeyDescriptor use="encryption">
            <ds:KeyInfo>
              <ds:X509Data>
                  <ds:X509Certificate>...</ds:X509Certificate>
              </ds:X509Data>
            </ds:KeyInfo>
        </KeyDescriptor>

        <NameIDFormat>urn:mace:shibboleth:1.0:nameIdentifier</NameIDFormat>
        <NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</NameIDFormat>

        <SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                             Location="https://HOST_NAME/cas/idp/profile/SAML2/POST/SLO"/>
        <SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
                             Location="https://HOST_NAME/cas/idp/profile/SAML2/POST/SSO"/>
        <SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
                             Location="https://HOST_NAME/cas/idp/profile/SAML2/Redirect/SSO"/>
    </IDPSSODescriptor>
</EntityDescriptor>
```

SAML2 identity provider metadata can be managed in dynamics ways 
as well. To learn more, please [review this guide](../installation/Configuring-SAML2-DynamicMetadata.html).

### Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata artifacts that would be applicable to a specific service definition and managed via the file system need to be stored
in a directory location named after the service definition's name and numeric identifier inside the canonical metadata directory. For example,
if global metadata artifacts are managed on disk at `/etc/cas/config/saml/metadata`, then metadata applicable to a service definition
whose name is configured as `SampleService` with an id of `1000` are expected to be found at `/etc/cas/config/saml/metadata/SampleService-1000`.

SAML2 identity provider metadata can be managed in dynamics ways 
as well. To learn more, please [review this guide](../installation/Configuring-SAML2-DynamicMetadata.html).

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

{% include {{ version }}/saml2-idp-configuration.md %}

### Session Replication

{% include casproperties.html properties="cas.session-replication" %}

### Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint          | Description
|-------------------|-------------------------------------------------------------------------------------------------------
| `samlPostProfileResponse` | Obtain a SAML2 response payload by supplying a `username`, `password` and `entityId` as parameters.

### SAML Services

Please [see this guide](../services/SAML2-Service-Management.html) to learn more 
about how to configure SAML2 service providers.

### Metadata Aggregates

CAS services are fundamentally recognized and loaded by service identifiers taught to CAS typically via
regular expressions. This allows for common groupings of applications and services by
url patterns (i.e. "Everything that belongs to `example.org` is registered with CAS).
With aggregated metadata, CAS essentially does double
authorization checks because it will first attempt to find the entity id
in its collection of resolved metadata components and then it looks to
see if that entity id is authorized via the pattern that is assigned to
that service definition. This means you can do one of several things:

1. Open up the pattern to allow everything that is authorized in the metadata.
2. Restrict the pattern to only a select few entity ids found in the
metadata. This is essentially the same thing as defining metadata criteria
to filter down the list of resolved relying parties and entity ids except that its done
after the fact once the metadata is fully loaded and parsed.
3. You can also instruct CAS to filter metadata
entities by a defined criteria at resolution time when it reads the
metadata itself. This is essentially the same thing as forcing the pattern
to match entity ids, except that it's done while CAS is reading the
metadata and thus load times are improved.

### Metadata Resolution

Service provider metadata is fetched and loaded on demand for every service and then cached in a global cache for a 
configurable duration. Subsequent requests for service metadata will always consult the cache first and if missed, 
will resort to actually resolving the metadata by loading or contacting the configured resource. 
Each service provider definition that is registered with CAS may optionally also specifically an expiration period of 
metadata resolution to override the default global value.

#### Dynamic Metadata Resolution

In addition to the more traditional means of managing service provider metadata such as direct XML files or URLs, CAS 
provides support for a number of other strategies to fetch metadata more dynamically with the likes of MDQ and more.
To learn more, please [review this guide](../installation/Configuring-SAML2-DynamicMetadata.html).

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

{% include {{ version }}/saml2-idp-configuration.md %}

## Client Libraries

For Java-based applications, the following frameworks may be used to integrate your application with CAS acting as a SAML2 identity provider:

- [Spring Security SAML](http://projects.spring.io/spring-security-saml/)
- [Pac4j](http://www.pac4j.org/docs/clients/saml.html)

## Sample Client Applications

- [Spring Security SAML Sample Webapp](https://github.com/cas-projects/saml2-sample-java-webapp)
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
