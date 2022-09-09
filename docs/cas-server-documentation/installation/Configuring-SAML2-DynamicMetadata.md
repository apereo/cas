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

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.core" %}

You may use [this service](https://www.samltool.com/idp_metadata.php) to experiment with the metadata generation process
and produce an example metadata for review and study.

## Metadata Management

Service provider or identity provider metadata can also be managed using the following strategies.

### File System

SAML2 identity provider metadata by default is generated on disk. 

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.file-system" %}

#### Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata artifacts that would be applicable to a specific service definition and managed via the file system need to be stored
in a directory location named after the service definition's name and numeric identifier inside the canonical metadata directory. For example,
if global metadata artifacts are managed on disk at `/etc/cas/config/saml/metadata`, then metadata applicable to a service definition
whose name is configured as `SampleService` with an id of `1000` are 
expected to be found at `/etc/cas/config/saml/metadata/SampleService-1000`.

### Advanced
            
Service provider or identity provider metadata can also be managed using any one of the following strategies. 

| Storage          | Description                                         
|--------------------------------------------------------------------------------------------------
| Metadata Query Protocol           | [See this guide](Configuring-SAML2-DynamicMetadata-MDQ.html).  
| HTTP/HTTPS                        | [See this guide](Configuring-SAML2-DynamicMetadata-HTTP.html).  
| REST                              | [See this guide](Configuring-SAML2-DynamicMetadata-REST.html).  
| Git                               | [See this guide](Configuring-SAML2-DynamicMetadata-Git.html).  
| MongoDb                           | [See this guide](Configuring-SAML2-DynamicMetadata-MongoDb.html).  
| Redis                             | [See this guide](Configuring-SAML2-DynamicMetadata-Redis.html).  
| JPA                               | [See this guide](Configuring-SAML2-DynamicMetadata-JPA.html).  
| CouchDb                           | [See this guide](Configuring-SAML2-DynamicMetadata-CouchDb.html).  
| Groovy                            | [See this guide](Configuring-SAML2-DynamicMetadata-Groovy.html).  
| Amazon S3                         | [See this guide](Configuring-SAML2-DynamicMetadata-AmazonS3.html).

## SAML Services

Please [see this guide](../services/SAML2-Service-Management.html) to learn more
about how to configure SAML2 service providers.
