---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# HTTP/HTTPS - SAML2 Metadata Management

A metadata location for a SAML service definition may download SAML2 metadata from http or https URLs.

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 1,
  "description" : "A Groovy-based metadata resolver",
  "metadataLocation" : "https://sp.example.org/saml2/metadata"
}
```

{% include casproperties.html properties="cas.authn.saml-idp.metadata.http" %}
