---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# Google Cloud Storage - SAML2 Metadata Management

Metadata documents may also be stored in and fetched from Google Cloud Storage.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="samlIdPRegisteredServiceMetadata" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider may also be managed and stored 
via Google Cloud Storage. Artifacts such as the metadata, signing and encryption keys, etc are kept
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
