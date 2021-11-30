---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# MongoDb - SAML2 Metadata Management

Metadata documents may also be stored in and fetched from a MongoDb instance.  This may specially be used to avoid
copying metadata files across CAS nodes in a cluster, particularly where one needs to deal with more than a few
bilateral SAML integrations. Metadata documents are stored in and fetched from a single pre-defined collection
that is taught to CAS via settings.  The outline of the document is as follows:

| Field       | Description                                                           |
|-------------|-----------------------------------------------------------------------|
| `id`        | The identifier of the record.                                         |
| `name`      | Indexed field which describes and names the metadata briefly.         |
| `value`     | The XML document representing the metadata for the service provider.  |
| `signature` | The contents of the signing certificate to validate metadata, if any. |

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-metadata-mongo" %}

SAML service definitions must then be designed as follows to allow CAS to fetch metadata documents from MongoDb instances:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "description" : "A MongoDb-based metadata resolver",
  "metadataLocation" : "mongodb://"
}
```

<div class="alert alert-info"><strong>Metadata Location</strong><p>
The metadata location in the registration record above needs to be specified as <code>mongodb://</code> to signal to CAS that 
SAML metadata for registered service provider must be fetched from MongoDb data sources defined in CAS configuration. 
</p></div>

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.mongo" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider may also be managed and stored via
MongoDb. Artifacts such as the metadata, signing and encryption keys, etc are kept inside a MongoDb
collection taught to CAS via settings as a single document that would have the following structure:

```json
{
    "signingCertificate": "...",
    "signingKey": "...",
    "encryptionCertificate": "...",
    "encryptionKey": "...",
    "metadata": "...",
    "appliesTo": "CAS"
}
```

## Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata documents that would be applicable to a service definition need to adjust the `appliesTo` field in the metadata
document to carry the service definition's name and numeric identifier using the `[service-name]-[service-numeric-identifier]` format.
