---
layout: default
title: CAS - SAML2 Attribute Release
category: Attributes
---
{% include variables.html %}

# SAML2 Attribute Definitions

[Attribute definitions](../integration/Attribute-Definitions.html) that specifically apply to the release of 
attributes as part of SAML response can be defined using the `SamlIdPAttributeDefinition`. Defining an attribute with this definition does not
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
    "friendlyName": "eduPersonPrincipalName",
    "persistent": false,
    "salt": "6jGzT@!nf0i3"
  }
}
```

The following additional settings can be specified for a SAML attribute definition:

| Name           | Description                                                                                                                                                                                           |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `friendlyName` | (Optional) Friendly name of the attribute shared with the target application during attribute release.                                                                                                |
| `urn`          | (Optional) Defined Universal Resource name for an attribute (i.e. `urn:oid:1.3.6.1.4.1.5923.1.1.1.6`).                                                                                                |
| `persistent`   | (Optional) Boolean flag to indicate whether the attribute value should be generated as a persistent value.                                                                                            |
| `salt`         | Required Salt value to use when creating persistent attribute definition values. This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. |

To learn more about attribute definitions, please [see this guide](../integration/Attribute-Definitions.html).
 
## Persistent Definitions

Attributes such as `eduPersonTargetedID` can be registered as a *persistent* attribute definition, allowing CAS to provide an opaque identifier 
for the username. This value is a tuple consisting of an opaque identifier for the principal, a name 
for the source of the identifier, and a name for the intended audience of the identifier.

```json
{
  "@class": "java.util.TreeMap",
  "eduPersonTargetedID": {
    "@class": "org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition",
    "key": "eduPersonTargetedID",
    "name": "eduPersonTargetedID",
    "urn": "urn:oid:1.3.6.1.4.1.5923.1.1.1.10",
    "persistent": true,
    "salt": "OqmG80fEKBQt",
    "friendlyName": "eduPersonTargetedID"
  }
}
```

## Defaults

By default, the following *known* attribute definitions are included and ship with CAS automatically:

| Name                          | URN                                  |
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
