---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# Redis - SAML2 Metadata Management

Metadata documents may also be stored in and fetched from a Redis instance.  This may specially be used
to avoid copying metadata files across CAS nodes in a cluster, particularly where one needs to
deal with more than a few bilateral SAML integrations.

Support is enabled by including the following module in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-metadata-redis" %}

SAML service definitions must then be designed as follows to allow CAS to fetch metadata documents from MongoDb instances:

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

<div class="alert alert-info"><strong>Metadata Location</strong><p>
The metadata location in the registration record above needs to be specified as <code>redis://</code> to signal to CAS that 
SAML metadata for registered service provider must be fetched from Redis data sources defined in CAS configuration. 
</p></div>

{% include casproperties.html properties="cas.authn.saml-idp.metadata.redis" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider may also be managed and stored via Redis.

## Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata documents that would be applicable to a service definition need to adjust the `appliesTo` field in the metadata
document to carry the service definition's name and numeric identifier using the `[service-name]-[service-numeric-identifier]` format.
