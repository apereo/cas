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

## Actuator Endpoints
        
The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="samlIdPRegisteredServiceMetadataCache,caches" %}


## Metadata Aggregates

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

## Metadata Resolution

Service provider metadata is fetched and loaded on demand for every service and then cached in a global cache for a
configurable duration. Subsequent requests for service metadata will always consult the cache first and if missed,
will resort to actually resolving the metadata by loading or contacting the configured resource.
Each service provider definition that is registered with CAS may optionally also specifically an expiration period of
metadata resolution to override the default global value.
 
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
