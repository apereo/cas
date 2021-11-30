---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# REST - SAML2 Metadata Management

Similar to the Dynamic Metadata Query Protocol (MDQ), SAML service provider metadata may also be fetched using a more traditional REST
interface. This is a simpler option that does not require one to deploy a compliant MDQ server and provides the
flexibility of producing SP metadata using any programming language or framework.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-metadata-rest" %}

Use the below snippet as an example to fetch metadata from REST endpoints:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "evaluationOrder" : 10,
  "metadataLocation" : "rest://"
}
```

<div class="alert alert-info"><strong>Metadata Location</strong><p>
The metadata location in the registration record above needs to be specified as <code>rest://</code> to signal to 
CAS that SAML metadata for registered service provider must be fetched from REST endpoints defined in CAS configuration.
</p></div>

Requests are submitted to REST endpoints with `entityId` as the parameter and `Content-Type: application/xml` as the header. Upon
a successful `200 - OK` response status, CAS expects the body of the HTTP response to match the below snippet:

```json
{  
   "id":1000,
   "name":"SAML Metadata For Service Provider",
   "value":"...",
   "signature":"..."
}
```

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.rest" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider
may also be managed and stored via REST APIs. Artifacts such as the metadata, signing and
encryption keys, etc are passed along to an external API
endpoint in the following structure as the request body:

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

The URL endpoint, defined in CAS settings is expected to be available at a path that ends in `/idp`, which is added onto the URL endpoint by CAS automatically.
The API is expected to produce a successful `200 - OK` response status on all operations outlined below:

| Method | Description                                                                                                                                                                                                                                                                                |
|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET`  | The response is expected to produce a JSON document outlining keys and metadata as indicated above. An `appliesTo` parameter may be passed to indicate the document owner and applicability, where a value of `CAS` indicates the CAS server as the global owner of the metadata and keys. |
| `POST` | Store the metadata and keys to finalize the metadata generation process. The request body contains the JSON document that outlines metadata and keys as indicated above.                                                                                                                   |

## Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata documents that would be applicable to a service definition need to adjust the `appliesTo` field in the metadata
document to carry the service definition's name and numeric identifier using the `[service-name]-[service-numeric-identifier]` format.
