---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# Amazon S3 - SAML2 Metadata Management

Metadata documents may also be stored in and fetched from an Amazon S3 instance.
This may specially be used to avoid copying metadata files across CAS nodes in a cluster, particularly where one needs 
to deal with more than a few bilateral SAML integrations. Metadata documents are stored in and fetched from a 
single pre-defined bucket that is taught to CAS via settings.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-metadata-aws-s3" %}

SAML service definitions must then be designed as follows to allow CAS to fetch metadata documents from Amazon S3 buckets:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp(s)",
  "name" : "SAMLService",
  "id" : 10000003,
  "description" : "Amazon S3-based metadata resolver",
  "metadataLocation" : "awss3://"
}
```

The following parameters are expected for the Amazon S3 object metadata:

| Parameter   | Description                               |
|-------------|-------------------------------------------|
| `signature` | The metadata signing certificate, if any. |

<div class="alert alert-info"><strong>Metadata Location</strong><p>
The metadata location in the registration record above needs to be specified as <code>awss3://</code> to signal to CAS that 
SAML metadata for registered service provider must be fetched from Amazon S3 defined in CAS configuration. 
</p></div>

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.amazon-s3" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider may also be managed and stored 
via Amazon S3 buckets. Artifacts such as the metadata, signing and encryption keys, etc are kept
inside a bucket with metadata that would have the following structure:

| Field                   | Description                                       |
|-------------------------|---------------------------------------------------|
| `id`                    | The identifier of the record.                     |
| `signingCertificate`    | The signing certificate.                          |
| `signingKey`            | The signing key.                                  |
| `encryptionCertificate` | The encryption certificate.                       |
| `encryptionKey`         | The encryption key.                               |
| `appliesTo`             | The owner of this metadata document (i.e. `CAS`). |

The actual object's content/body is expected to contain the SAML2 identity provider metadata. Note 
that the signing and encryption keys are expected to be encrypted and signed using CAS crypto keys.

### Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata documents that would be applicable to a service definition need to be put in a special bucket named 
using the `[service-name][service-numeric-identifier]` format.
