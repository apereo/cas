---
layout: default
title: CAS - SAML2 Authentication
---

# SAML2 Authentication

CAS can act as a SAML2 identity provider accepting authentication requests and producing SAML assertions.

## Overview

### SAML Endpoints
The following CAS endpoints respond to supported SAML2 profiles:

- `/cas/idp/profile/SAML2/Redirect/SSO`
- `/cas/idp/profile/SAML2/POST/SSO`
- `/cas/idp/profile/SAML2/POST/SLO`

### IdP Metadata
The following CAS endpoints handle the generation of SAML2 metadata:
 
- `/cas/idp/metadata`

This endpoint will display the CAS IdP SAML2 metadata upon receiving a GET request. If metadata is already available and generated,
it will be displayed. If metadata is absent, one will be generated automatically. 
CAS configuration below dictates where metadata files/keys will be generated and stored.

<div class="alert alert-info"><strong>Review Metadata</strong><p>Due to the way CAS handles the generation of metadata via external 
libraries, the generated metadata MUST be reviewed and massaged slightly to match the CAS configuration. All other elements MUST be 
removed.</p></div>

Here is a generated metadata file as an example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<EntityDescriptor  xmlns="urn:oasis:names:tc:SAML:2.0:metadata" xmlns:ds="http://www.w3.org/2000/09/xmldsig#" 
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

### SP Metadata
- `/cas/idp/servicemetadatagen`

This endpoint will attempt to generate metadata for relying party upon receiving a POST request. This is useful when integrating with
service providers that do not publish a defined metadata. The following parameters are expected by this point:

| Parameter                         | Description
|-----------------------------------+-----------------------------------------+
| `entityId`                        | Required.
| `authnRequestSigned`              | Optional. Defaults to `false`.
| `wantAssertionsSigned`            | Optional. Defaults to `false`.
| `x509Certificate`                 | Required.
| `acsUrl`                          | Required.

## Components
Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-saml-idp</artifactId>
  <version>${cas.version}</version>
</dependency>
```

## Configuration

### Settings

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).


### SAML Services
SAML relying parties and services must be registered within the CAS service registry similar to the following example:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "evaluationOrder" : 10,
  "metadataLocation" : "http://www.testshib.org/metadata/testshib-providers.xml"
}
```


<div class="alert alert-info"><strong>Aggregated Metadata</strong><p>If metadata 
contains data for more than one relying party, (i.e. InCommon) those relying parties need to be defined by their entity id, explicitly via 
the <code>serviceId</code> field. </p></div>

The following fields are available for SAML services:

| Field                                | Description
|--------------------------------------+-----------------------------------------------------------------+
| `metadataLocation`                   | Location of service metadata defined from system files, classpath or URL resources. 
| `metadataSignatureLocation`          | Location of the metadata *public key* to validate the metadata which must be defined from system files or classpath. If defined, will enforce the `SignatureValidationFilter` validation filter on metadata.
| `metadataMaxValidity`                | If defined, will enforce the `RequiredValidUntilFilter` validation filter on metadata.
| `signAssertions`                     | Whether assertions should be signed. Default is `false`.
| `signResponses`                      | Whether responses should be signed. Default is `true`.
| `encryptAssertions`                  | Whether assertions should be encrypted. Default is `false`.
| `requiredAuthenticationContextClass` | If defined, will specify the SAML authentication context class in the final response. If undefined, the authentication class will either be `urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified` or `urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport` depending on the SAML authentication request. 

### Dynamic Metadata
CAS also supports the [Dynamic Metadata Query Protocol](https://spaces.internet2.edu/display/InCFederation/Metadata+Query+Protocol)
which is a REST-like API for requesting and receiving arbitrary metadata. In order to configure a CAS SAML service to retrieve its metadata
from a Metadata query server, the metadata location must be configured to point to the query server instance. Here is an example:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "evaluationOrder" : 10,
  "metadataLocation" : "http://mdq.server.org/entities/{0}"
}
```

...where `{0}` serves as an entityID placeholder for which metadata is to be queried. 






