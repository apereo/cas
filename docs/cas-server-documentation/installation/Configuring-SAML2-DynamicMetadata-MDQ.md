---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# Metadata Query Protocol - SAML2 Metadata Management

CAS also supports the [Metadata Query Protocol](https://spaces.at.internet2.edu/display/MDQ/Metadata+Query+Protocol),
also known as `MDQ`, which is a REST-like API for requesting and receiving
arbitrary metadata. In order to configure a CAS SAML service to retrieve its
metadata from a Metadata query server, the metadata location must be configured to point to the query server instance.

MDQ may be configured using the below snippet as an example:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "evaluationOrder" : 10,
  "metadataLocation" : "https://mdq.server.org/entities/{0}"
}
```

...where `{0}` serves as an entityID placeholder for which metadata is to be queried. The placeholder
is dynamically processed and replaced by CAS at runtime.

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.mdq" %}
