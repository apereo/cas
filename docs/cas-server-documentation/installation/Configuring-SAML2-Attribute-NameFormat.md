---
layout: default
title: CAS - SAML2 Attribute Release
category: Attributes
---
{% include variables.html %}

# SAML2 Attribute Name Formats

Attribute name formats can be specified per relying party in the service registry.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name": "SAML Service",
  "metadataLocation" : "../../sp-metadata.xml",
  "id": 100001,
  "attributeNameFormats": {
    "@class": "java.util.HashMap",
    "attributeName": "basic|uri|unspecified|custom-format-etc"
  }
}
```

Name formats for an individual attribute can be mapped to a
number of pre-defined formats, or a custom format of your own choosing.
A given attribute that is to be encoded in the final SAML response
may contain any of the following name formats:

| Type                | Description                                                                     |
|---------------------|---------------------------------------------------------------------------------|
| `basic`             | Map the attribute to `urn:oasis:names:tc:SAML:2.0:attrname-format:basic`.       |
| `uri`               | Map the attribute to `urn:oasis:names:tc:SAML:2.0:attrname-format:uri`.         |
| `unspecified`       | Map the attribute to `urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified`. |
| `urn:my:own:format` | Map the attribute to `urn:my:own:format`.                                       |

You may also have the option to define attributes and their relevant name format globally
via CAS properties. 

{% include_cached casproperties.html properties="cas.authn.saml-idp.core" %}
