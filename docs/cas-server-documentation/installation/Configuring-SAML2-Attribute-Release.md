---
layout: default
title: CAS - SAML2 Attribute Release
category: Attributes
---
{% include variables.html %}


# SAML2 Attribute Release

Attribute filtering and release policies are defined per 
SAML service. See [this guide](../integration/Attribute-Release-Policies.html) for more info.

## Attribute Definitions

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

## Attribute Value Types

[See this guide](Configuring-SAML2-Attribute-ValueTypes.html).

## Attribute Name Formats

[See this guide](Configuring-SAML2-Attribute-NameFormat.html).

## Attribute Friendly Names

[See this guide](Configuring-SAML2-Attribute-FriendlyName.html).

## InCommon Research and Scholarship

A specific attribute release policy is available to release 
the attribute bundles needed for InCommon Research and Scholarship service providers using the entity 
attribute value `http://id.incommon.org/category/research-and-scholarship`:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/incommon/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [ "java.util.ArrayList",
      [
         {
           "@class": "org.apereo.cas.support.saml.services.InCommonRSAttributeReleasePolicy",
           "useUniformResourceName": false
         }
      ]
    ]
  }
}
```

Attributes authorized for release are set to be `eduPersonPrincipalName`, `eduPersonTargetedID`, `email`, `displayName`, 
`givenName`, `surname`, `eduPersonScopedAffiliation`.

## REFEDS Research and Scholarship

A specific attribute release policy is available to release the [attribute bundles](https://refeds.org/category/research-and-scholarship)
needed for REFEDS Research and Scholarship service providers using 
the entity attribute value `http://refeds.org/category/research-and-scholarship`:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/incommon/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [ "java.util.ArrayList",
      [
         {"@class": "org.apereo.cas.support.saml.services.RefedsRSAttributeReleasePolicy"}
      ]
    ]
  }
}
```

This policy is an extension of `InCommonRSAttributeReleasePolicy` that operates based on different entity attribute value.

## Releasing `eduPersonTargetedID`

If you do not have pre-calculated values for the `eduPersonTargetedID` attribute to fetch before release, 
you can let CAS calculate the `eduPersonTargetedID` attribute dynamically at release time using the following policy:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.EduPersonTargetedIdAttributeReleasePolicy",
    "salt": "OqmG80fEKBQt",
    "attribute": "",
    "useUniformResourceName": false
  }
}
```

The generated id may be based off of an existing principal attribute. If left unspecified or attribute not found, 
the authenticated principal id is used.

## Groovy Script

This policy allows a Groovy script to calculate the collection of released attributes.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/incommon/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.GroovySamlRegisteredServiceAttributeReleasePolicy",
    "groovyScript": "file:/etc/cas/config/script.groovy"
  }
}
```

The configuration of this component qualifies to use 
the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

The outline of the script may be designed as:

```groovy
import java.util.*
import org.apereo.cas.support.saml.services.*
import org.apereo.cas.support.saml.*

Map<String, Object> run(final Object... args) {
    def attributes = args[0]
    def service = args[1]
    def resolver = args[2]
    def facade = args[3]
    def entityDescriptor = args[4]
    def applicationContext = args[5]
    def logger = args[6]
    ...
    return null;
}
```

The following parameters are passed to the script:

| Parameter            | Description                                                                           |
|----------------------|---------------------------------------------------------------------------------------|
| `attributes`         | Map of current attributes resolved and available for release.                         |
| `service`            | The SAML service definition matched in the service registry.                          |
| `resolver`           | The metadata resolver instance of this service provider.                              |
| `facade`             | A wrapper on top of the metadata resolver that allows access to utility functions.    |
| `entityDescriptor`   | The `EntityDescriptor` object matched and linked to this service provider's metadata. |
| `applicationContext` | CAS application context allowing direct access to beans, etc.                         |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`.           |

An example script follows:

```groovy
import java.util.*
import org.apereo.cas.support.saml.services.*
import org.apereo.cas.support.saml.*

def Map<String, Object> run(final Object... args) {
    def attributes = args[0]
    def service = args[1]
    def resolver = args[2]
    def facade = args[3]
    def entityDescriptor = args[4]
    def applicationContext = args[5]
    def logger = args[6]

    if (entityDescriptor.entityId == "TestingSAMLApplication") {
      return [username:["something"], another:"attribute"]
    }
    return [:]
}
```

## Pattern Matching Entity Ids

In the event that an aggregate is defined containing multiple entity ids, the below attribute release 
policy may be used to release a collection of allowed attributes to entity ids grouped together by a regular expression pattern:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/incommon/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.PatternMatchingEntityIdAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
    "fullMatch" : "true",
    "reverseMatch" : "false",
    "entityIds" : "entityId1|entityId2|somewhere.+"
  }
}
```

## Entity Attributes Filter

This attribute release policy authorizes the release of defined attributes, provided the accompanying 
metadata for the service provider contains attributes that match certain values.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.MetadataEntityAttributesAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
    "entityAttributeValues" : [ "java.util.LinkedHashSet", [ "entity-attribute-value" ] ],
    "entityAttribute" : "http://somewhere.org/category-x",
    "entityAttributeFormat" : "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"
  }
}
```

The specification of `entityAttributeFormat` is optional.

## Metadata Requested Attributes

This attribute release policy authorizes the release of defined attributes, based on the accompanying 
metadata for the service provider having requested attributes as part of its `AttributeConsumingService` element.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.MetadataRequestedAttributesAttributeReleasePolicy",
    "useFriendlyName" : false
  }
}
```

The `useFriendlyName` allows the filter to compare the requested attribute's friendly name with the resolved attribute.

## Metadata Registration Authority

This attribute release policy authorizes the release of a subset of attributes if the registration authority
specified as a metadata extension produces a successful match.

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.MetadataRegistrationAuthorityAttributeReleasePolicy",
    "registrationAuthority" : "urn:example:.*",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ]
  }
}
```

The `registrationAuthority` is a regular expression that is matched against the registration authority of the 
`RegistrationInfo` element to authorize release of allowed attributes.


## Authentication Request Requested Attributes Filter

This attribute release policy authorizes the release of a subset of attributes requested as extensions of
the SAML2 authentication request. The intersection of requested attributes and those allowed by 
the attribute release policy explicitly is evaluated for the final attribute release phase:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.AuthnRequestRequestedAttributesAttributeReleasePolicy",
    "useFriendlyName" : false
  }
}
```

The `useFriendlyName` allows the filter to compare the requested attribute’s friendly name with the resolved attribute.
