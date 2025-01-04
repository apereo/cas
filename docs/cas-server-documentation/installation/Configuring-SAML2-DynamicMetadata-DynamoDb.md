---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# DynamoDb - SAML2 Metadata Management

Metadata documents may also be stored in and fetched from an Amazon DynamoDb instance.
This may specially be used to avoid copying metadata files across CAS nodes in a cluster, particularly where one needs 
to deal with more than a few bilateral SAML integrations. Metadata documents are stored in and fetched from a 
single pre-defined bucket that is taught to CAS via settings.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-metadata-dynamodb" %}

SAML service definitions must then be designed as follows to allow CAS to fetch metadata documents from Amazon DynamoDb:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp(s)",
  "name" : "SAMLService",
  "id" : 10000003,
  "description" : "Amazon DynamoDb metadata resolver",
  "metadataLocation" : "dynamodb://"
}
```

<div class="alert alert-info">:information_source: <strong>Metadata Location</strong><p>
The metadata location in the registration record above needs to be specified as <code>dynamodb://</code> to signal to CAS that 
SAML metadata for registered service provider must be fetched from Amazon DynamoDb defined in CAS configuration. 
</p></div>

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.dynamo-db" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider may also be managed and stored 
via Amazon DynamoDb. Artifacts such as the metadata, signing and encryption keys, etc are kept
inside a bucket that carries a *JSON document* with the following fields:

| Field                   | Description                                       |
|-------------------------|---------------------------------------------------|
| `signingCertificate`    | The signing certificate.                          |
| `signingKey`            | The signing key.                                  |
| `encryptionCertificate` | The encryption certificate.                       |
| `encryptionKey`         | The encryption key.                               |
| `appliesTo`             | The owner of this metadata document (i.e. `CAS`). |
| `metadata`              | The metadata document.                            |

Note that the signing and encryption keys are expected to be encrypted and signed using CAS crypto keys.

### Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata documents that would be applicable to a service definition need to be put in a special bucket named 
using the `[service-name][service-numeric-identifier]` format.
