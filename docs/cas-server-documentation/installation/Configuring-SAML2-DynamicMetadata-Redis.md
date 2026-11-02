---
layout: default
title: CAS - Redis - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# Redis - SAML2 Metadata Management

Metadata documents may also be stored in and fetched from a Redis instance.  This may specially be used
to avoid copying metadata files across CAS nodes in a cluster, particularly where one needs to
deal with more than a few bilateral SAML integrations.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-metadata-redis" %}

SAML service definitions must then be designed as follows to allow CAS to fetch metadata documents from Redis instances:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "description" : "A Redis-based metadata resolver",
  "metadataLocation" : "redis://"
}
```

<div class="alert alert-info">:information_source: <strong>Metadata Location</strong><p>
The metadata location in the registration record above needs to be specified as <code>redis://</code> to signal to CAS that 
SAML metadata for registered service provider must be fetched from Redis data sources defined in CAS configuration. 
</p></div>

Next to the metadata documents, CAS keeps an index of the documents of each entity, so that the metadata of a service
provider is found without scanning every key in Redis. Documents stored by earlier CAS versions are indexed once, the first
time a service provider is looked up. Documents should therefore be managed through CAS, such as with the actuator endpoint
below, rather than written to Redis directly.

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.redis" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="samlIdPRegisteredServiceMetadata" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider may also be managed and stored via Redis.

## Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata documents that would be applicable to a service definition need to adjust the `appliesTo` field in the metadata
document to carry the service definition's name and numeric identifier using the `[service-name]-[service-numeric-identifier]` format.
