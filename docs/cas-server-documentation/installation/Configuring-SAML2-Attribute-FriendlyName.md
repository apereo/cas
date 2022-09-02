---
layout: default
title: CAS - SAML2 Attribute Release
category: Attributes
---
{% include variables.html %}

# SAML2 Attribute Friendly Name

Attribute friendly names can be specified per relying party in the service registry, as well as globally via CAS settings.
If there is no friendly name defined for the attribute, the
attribute name will be used instead in its place. Note that the name of the
attribute is one that is designed to be released to the service provider,
specially if the original attribute is *mapped* to a different name.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation" : "../../sp-metadata.xml",
  "id": 100001,
  "attributeFriendlyNames": {
    "@class": "java.util.HashMap",
    "urn:oid:2.5.4.42": "friendly-name-to-use"
  }
}
```

You may also have the option to define attribute friendly names globally via CAS properties.

{% include_cached casproperties.html properties="cas.authn.saml-idp.core" %}
