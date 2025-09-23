---
layout: default
title: CAS - SAML2 Attribute Release
category: Attributes
---
{% include variables.html %}

# SAML2 Attribute Release

Attribute filtering and release policies are defined per 
SAML service. See [this guide](../integration/Attribute-Release-Policies.html) for more info.

{% tabs saml2attrrel %}

{% tab saml2attrrel InCommon Research and Scholarship %}

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

Attributes authorized for release are set to be `eduPersonPrincipalName`, `eduPersonTargetedID`, `mail`, `displayName`,
`givenName`, `surname`, `eduPersonScopedAffiliation`.

{% endtab %}

{% tab saml2attrrel REFEDS Research and Scholarship %}

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

{% endtab %}

{% tab saml2attrrel eduPersonTargetedID %}

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
the authenticated principal id is used. You can also control whether the final generated attribute should be named
`urn:oid:1.3.6.1.4.1.5923.1.1.1.10` or `eduPersonTargetedID` via the `useUniformResourceName` setting.

{% endtab %}

{% tab saml2attrrel <i class="fa fa-file-code px-1"></i>Groovy %}

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
    def (attributes,service,resolver,facade,entityDescriptor,applicationContext,logger) = args
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

def run(final Object... args) {
    def (attributes,service,resolver,facade,entityDescriptor,applicationContext,logger) = args
    if (entityDescriptor.entityId == "TestingSAMLApplication") {
      return [username:["something"], another:"attribute"]
    }
    return [:]
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab saml2attrrel Pattern Matching Entity Ids %}

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
    "fullMatch" : true,
    "reverseMatch" : false,
    "entityIds" : "entityId1|entityId2|somewhere.+"
  }
}
```

{% endtab %}

{% tab saml2attrrel Entity Attributes %}

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

{% endtab %}

{% tab saml2attrrel Metadata Requested Attributes %}

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

{% endtab %}

{% tab saml2attrrel Metadata Registration Authority %}

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

{% endtab %}

{% tab saml2attrrel Authentication Request Requested Attributes %}

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

{% endtab %}

{% tab saml2attrrel Authentication Request Requester ID %}

This attribute release policy authorizes the release of allowed attributes if the requester ID of the
SAML2 authentication request inside its `Scoping` element matches the defined pattern:

```json
{
  "@class": "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId": "entity-ids-allowed-via-regex",
  "name": "SAML",
  "id": 10,
  "metadataLocation": "path/to/metadata.xml",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.support.saml.services.AuthnRequestRequesterIdAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ]
    "requesterIdPattern" : ".*"
  }
}
```

{% endtab %}

{% tab saml2attrrel <i class="fa fa-user-secret px-1"></i>Anonymous Access %}

A specific attribute release policy is available to release the [attribute bundles](https://refeds.org/category/anonymous)
to service providers that contain the entity attribute value `https://refeds.org/category/anonymous`:

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
         {"@class": "org.apereo.cas.support.saml.services.AnonymousAccessAttributeReleasePolicy"}
      ]
    ]
  }
}
```

{% endtab %}

{% tab saml2attrrel Pseudonymous Access %}

A specific attribute release policy is available to release the [attribute bundles](https://refeds.org/category/pseudonymous)
to service providers that contain the entity attribute value `https://refeds.org/category/pseudonymous`:

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
         {"@class": "org.apereo.cas.support.saml.services.PseudonymousAccessAttributeReleasePolicy"}
      ]
    ]
  }
}
```

{% endtab %}

{% tab saml2attrrel Personalized Access %}

A specific attribute release policy is available to release the [attribute bundles](https://refeds.org/category/personalized)
to service providers that contain the entity attribute value `https://refeds.org/category/personalized`:

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
         {"@class": "org.apereo.cas.support.saml.services.PersonalizedAccessAttributeReleasePolicy"}
      ]
    ]
  }
}
```

{% endtab %}

{% tab saml2attrrel <i class="fa fa-user-group px-1"></i> Entity Group %}

A specific attribute release policy that will compare the defined group as a regular expression with the name of the
`EntitiesDescriptor` element as well as any `AffiliationDescriptor` owners or identifiers.

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
         {"@class": "org.apereo.cas.support.saml.services.MetadataEntityGroupAttributeReleasePolicy"}
      ]
    ]
  }
}
```

{% endtab %}

{% endtabs %}
