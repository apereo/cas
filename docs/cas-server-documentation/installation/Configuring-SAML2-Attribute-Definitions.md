---
layout: default
title: CAS - SAML2 Attribute Release
category: Attributes
---
{% include variables.html %}

# SAML2 Attribute Definitions

Attribute definitions that specifically apply to the release of attributes as part of SAML response can be
defined using the `SamlIdPAttributeDefinition`. Defining an attribute with this definition does not
prevent it from being released by other protocols.

```json
{
  "@class": "java.util.TreeMap",
  "eduPersonPrincipalName": {
    "@class": "org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition",
    "key": "eduPersonPrincipalName",
    "name": "eduPersonPrincipalName",
    "urn": "urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
    "scoped": true,
    "encrypted": false,
    "attribute": "uid",
    "friendlyName": "eduPersonPrincipalName"
  }
}
```

The following additional settings can be specified for a Saml IdP attribute definition:

| Name           | Description                                                                                            |
|----------------|--------------------------------------------------------------------------------------------------------|
| `friendlyName` | (Optional) Friendly name of the attribute shared with the target application during attribute release. |
| `urn`          | (Optional) Defined Universal Resource name for an attribute (i.e. `urn:oid:1.3.6.1.4.1.5923.1.1.1.6`). |

To learn more about attribute definitions, please [see this guide](../integration/Attribute-Definitions.html).
    
## Defaults

By default, the following *known* attribute definitions are included and ship with CAS:

| Friendly Name                 | Key                                  |
|-------------------------------|--------------------------------------|
| `uid`                         | `urn:oid:0.9.2342.19200300.100.1.1`  |            
| `title`                       | `urn:oid:2.5.4.12`                   |
| `sn`                          | `urn:oid:2.5.4.4`                    |
| `surname`                     | `urn:oid:2.5.4.4`                    |
| `givenName`                   | `urn:oid:2.5.4.42`                   |
| `telephoneNumber`             | `urn:oid:2.5.4.20`                   |
| `email`                       | `urn:oid:0.9.2342.19200300.100.1.3`  |
| `mail`                        | `urn:oid:0.9.2342.19200300.100.1.3 ` |
| `commonName`                  | `urn:oid:2.5.4.3`                    |
| `displayName`                 | `urn:oid:2.16.840.1.113730.3.1.241`  |
| `organizationName`            | `urn:oid:2.5.4.10`                   |
| `eduPersonPrimaryAffiliation` | `urn:oid:1.3.6.1.4.1.5923.1.1.1.5`   |
| `eduPersonAffiliation`        | `urn:oid:1.3.6.1.4.1.5923.1.1.1.1`   |
| `eduPersonTargetedID`         | `urn:oid:1.3.6.1.4.1.5923.1.1.1.10`  |
| `eduPersonEntitlement`        | `urn:oid:1.3.6.1.4.1.5923.1.1.1.7`   |
| `eduPersonPrincipalName`      | `urn:oid:1.3.6.1.4.1.5923.1.1.1.6`   |
| `eduPersonScopedAffiliation`  | `urn:oid:1.3.6.1.4.1.5923.1.1.1.9`   |
| `eduPersonUniqueId`           | `urn:oid:1.3.6.1.4.1.5923.1.1.1.13`  |
| `eduPersonAssurance`          | `urn:oid:1.3.6.1.4.1.5923.1.1.1.11`  |
| `eduPersonNickname`           | `urn:oid:1.3.6.1.4.1.5923.1.1.1.2`   |
| `eduPersonOrcid`              | `urn:oid:1.3.6.1.4.1.5923.1.1.1.14`  |
        
