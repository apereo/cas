---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# SAML2 Metadata Management

The following CAS endpoints handle the generation of SAML2 metadata:

- `/idp/metadata`

This endpoint will display the CAS IdP SAML2 metadata upon receiving a GET request. If metadata is already available and generated,
it will be displayed. If metadata is absent, one will be generated automatically.
CAS configuration below dictates where metadata files/keys will be generated and stored.

Note that the endpoint can accept a `service` parameter either by entity id or numeric identifier. This parameter
is matched against the CAS service registry allowing the endpoint to calculate and combine any identity provider
metadata overrides that may have been specified.

{% include casproperties.html properties="cas.authn.saml-idp.metadata.core" %}

You may use [this service](https://www.samltool.com/idp_metadata.php) to experiment with the metadata generation process
and produce an example metadata for review and study.

## Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                                 | Description
|------------------------------------------|--------------------------------------------------------------------------------------
| `samlIdPRegisteredServiceMetadataCache`  | Manage and control the cache that holds metadata instances for SAML service providers. Note the cache is specific to the JVM memory of the CAS server node and it's **NOT** distributed or replicated. A `GET` operation produces the cached copy of the metadata for a given service provider, using the `serviceId` and `entityId` parameters. The `serviceId` parameter may be the numeric identifier for the registered service or its name. In case the service definition represents a metadata aggregate such as InCommon, the `entityId` parameter may be used to pinpoint and filter the exact entity within the aggregate. A `DELETE` operation will delete invalidate the metadata cache. If no parameters are provided, the metadata cache will be entirely invalidated. A `serviceId` parameter will force CAS to only invalidate the cached metadata instance for that service provider. The `serviceId` parameter may be the numeric identifier for the registered service or its name.

## File System

SAML2 identity provider metadata by default is generated on disk. 

{% include casproperties.html properties="cas.authn.saml-idp.metadata.file-system" %}

### Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata artifacts that would be applicable to a specific service definition and managed via the file system need to be stored
in a directory location named after the service definition's name and numeric identifier inside the canonical metadata directory. For example,
if global metadata artifacts are managed on disk at `/etc/cas/config/saml/metadata`, then metadata applicable to a service definition
whose name is configured as `SampleService` with an id of `1000` are 
expected to be found at `/etc/cas/config/saml/metadata/SampleService-1000`.

## Metadata Query Protocol

Please see [this guide](Configuring-SAML2-DynamicMetadata-MDQ.html).

## HTTP/HTTPS

Please see [this guide](Configuring-SAML2-DynamicMetadata-HTTP.html).

## REST

Please see [this guide](Configuring-SAML2-DynamicMetadata-REST.html).

## Git

Please see [this guide](Configuring-SAML2-DynamicMetadata-Git.html).

## MongoDb

Please see [this guide](Configuring-SAML2-DynamicMetadata-MongoDb.html).

## Redis

Please see [this guide](Configuring-SAML2-DynamicMetadata-Redis.html).

## JPA

Please see [this guide](Configuring-SAML2-DynamicMetadata-JPA.html).

## CouchDb

Please see [this guide](Configuring-SAML2-DynamicMetadata-CouchDb.html).

## Groovy

Please see [this guide](Configuring-SAML2-DynamicMetadata-Groovy.html).

## Amazon S3

Please see [this guide](Configuring-SAML2-DynamicMetadata-AmazonS3.html).
