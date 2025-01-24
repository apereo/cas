---
layout: default
title: CAS - Attribute Definitions 
category: Attributes
---

{% include variables.html %}

# Attribute Definitions

The definition of an attribute in CAS, when fetched and resolved from an authentication or attribute repository source, tends to be defined
and referenced using its name without any additional *metadata* or decorations. For example, you may wish to retrieve a `uid` attribute and virtually
rename and map it to a `userIdentifier` attribute either globally or for specific application integrations. For most use cases, this configuration
works quite comfortably and yet, depending on the nature of the target application and the authentication protocol used to complete the integration,
additional requirements could be imposed and may have to be specified to define an attribute with 
additional pointers, when shared and released with a relying party. For example, a 
SAML2 service provider may require a *scoped* attribute for an `eduPersonPrincipalName` whose value 
is always determined from the `uid` attribute with a special friendly-name that is always provided regardless of the target application. 

While bits and pieces of metadata about a given attribute can be defined either globally in CAS configuration settings 
or defined inside a service definition, an attribute definition store allows one to describe metadata about necessary attributes 
with special decorations to be considered during attribute resolution and release. The specification of the attribute definition store is entirely 
optional and the store may not contain any attribute definitions.

{% include_cached casproperties.html properties="cas.authn.attribute-repository.attribute-definition-store" %}
   
## Actuator Endpoint

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="attributeDefinitions" casModule="cas-server-support-reports" %}

## JSON Attribute Definitions

Attribute definitions may be defined inside a JSON file whose location is provided via CAS settings. The structure of the JSON file
may match the following:

```json 
{
    "@class" : "java.util.TreeMap",
    "employeeId" : {
      "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
      "key" : "employeeId",
      "scoped" : true,
      "attribute" : "empl_identifier"
    }
}
```         

Generally-speaking, attribute definitions are specified using a `Map` whose key is the 
attribute name, as resolved by the CAS [attribute resolution engine](Attribute-Resolution.html).
The attribute name as the key to the `Map` must match the `key` attribute of the attribute definition itself.
If the attribute in question is not already resolved as principal attribute with a valid set of values,
it might be possible, depending on the [attribute release policy](Attribute-Release.html), to 
resolve and create that attribute on the fly as an attribute definition that can produce values. 

<div class="alert alert-info">:information_source: <strong>Authorization</strong><p>
Please note that as of this writing, attribute definitions cannot be used to drive authorization decisions via the likes of RBAC.
Such definitions are typically evaluated during the attribute release phase which is too late for authorization decisions. If you need to
produce attributes specifically for authorization decisions, consider defining a specific attribute repository which would be evaluated
during the attribute resolution phase instead.</p></div>

The following settings can be specified by an attribute definition:

| Name                   | Description                                                                                                                                                                                                   |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `key`                  | Attribute name, as resolved by the CAS [attribute resolution engine](Attribute-Resolution.html).                                                                                                              |
| `name`                 | Comma-separated list of attribute name(s) to virtually rename/remap and share with the target application during attribute release.                                                                           |
| `scoped`               | (Optional) If `true`, the attribute value will be scoped to the scope of the CAS server deployment defined in settings.                                                                                       |
| `encrypted`            | (Optional) If `true`, the attribute value will be encrypted and encoded in base-64 using the service definition's defined public key.                                                                         |
| `attribute`            | (Optional) The source attribute to provide values for the attribute definition itself, replacing that of the original source.                                                                                 |
| `patternFormat`        | (Optional) Template used in a `java.text.MessageFormat` to decorate the attribute values.                                                                                                                     |
| `script`               | (Optional) Groovy script, external or embedded to process and produce attributes values. This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. |
| `canonicalizationMode` | (Optional) Control transformation of attribute values; allowed values are `UPPER`, `LOWER` or `NONE`.                                                                                                         |
| `patterns`             | (Optional) A map of regular expression patterns to static/dynamic constructs to build values, in scenarios where the attribute definition is built off of an existing attribute.                              |
| `flattened`            | (Optional) Indicate whether attribute definitions with multiple values should be flattened into a single value, separated by the assigned delimiter.                                                          |
| `singleValue`          | (Optional) Default is `false`. Determines if the attribute should be produced as a single-value claim if it has only a single value.                                                                          |
| `hashingStrategy`      | (Optional) Attempts to *hash* the attribute value based on `hex`, `base64`, `sha1`, `sha256` or `sha512` hashing function.                                                                                    |

The following operations in the order given should take place, if an attribute definition is to produce values:

- Produce attribute values based on the `attribute` setting specified in the attribute definition, if any.
- Produce attribute values based on the `script` setting specified in the attribute definition, if any.
- Produce attribute values based on the `patterns` setting specified in the attribute definition, if any.
- Produce attribute values based on the `scoped` setting specified in the attribute definition, if any.
- Produce attribute values based on the `patternFormat` setting specified in the attribute definition, if any.
- Produce attribute values based on the `hashingStrategy` setting specified in the attribute definition, if any.
- Produce attribute values based on the `encrypted` setting specified in the attribute definition, if any.
- Produce attribute values based on the `canonicalizationMode` setting specified in the attribute definition, if any.
- Produce attribute values based on the `flattened` setting specified in the attribute definition, if any.

{% tabs attrdefinitions %}

{% tab attrdefinitions Basic %}

Define an attribute definition for `employeeId` to produce scoped attributes
based on another attribute `empl_identifier` as the source:

```json 
{
    "@class" : "java.util.TreeMap",
    "employeeId" : {
      "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
      "key" : "employeeId",
      "scoped" : true,
      "attribute" : "empl_identifier"
    }
}
```  

Now that the definition is available globally, the attribute [can then be released](Attribute-Release-Policies.html)
as usual with the following definition:

```json
...
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "employeeId" ] ]
  }
...
```

{% endtab %}


{% tab attrdefinitions <i class="fa fa-mask px-1"></i> Encrypted %}
Same use case as above, except the attribute value will be encrypted and encoded using the service definition's public key:

```json 
{
    "@class" : "java.util.TreeMap",
    "employeeId" : {
      "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
      "key" : "employeeId",
      "encrypted" : true,
      "attribute" : "empl_identifier"
    }
}
```  

The service definition should have specified a public key definition:

```json
...
  "publicKey" : {
    "@class" : "org.apereo.cas.services.RegisteredServicePublicKeyImpl",
    "location" : "classpath:public.key",
    "algorithm" : "RSA"
  }
...
```

The keys can be generated via the following commands:

```bash
openssl genrsa -out private.key 1024
openssl rsa -pubout -in private.key -out public.key -inform PEM -outform DER
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in private.key -out private.p8
```
{% endtab %}

{% tab attrdefinitions Pattern Format %}
Define an attribute definition to produce values based on a pattern format:

```json 
{
    "@class" : "java.util.TreeMap",
    "eduPersonPrincipalName" : {
      "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
      "key" : "eduPersonPrincipalName",
      "name" : "urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
      "friendlyName" : "eduPersonPrincipalName",
      "scoped" : true,
      "patternFormat": "hello,{0}",
      "attribute" : "uid"
    }
}
```

If the resolved set of attributes are `uid=[test1, test2]` and the CAS server has a scope of `example.org`,
the final values of `eduPersonPrincipalName` would be [`hello,test1@example.org`,`hello,test2@example.org`]
released as `urn:oid:1.3.6.1.4.1.5923.1.1.1.6` with a friendly name of `eduPersonPrincipalName`.

{% endtab %}

{% tab attrdefinitions <i class="fa fa-pencil px-1"></i>Embedded Script %}
Same use case as above, except the attribute value be additional processed by an embedded Groovy script

```json 
{
    "@class" : "java.util.TreeMap",
    "eduPersonPrincipalName" : {
      "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
      "key" : "eduPersonPrincipalName",
      "name" : "urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
      "friendlyName" : "eduPersonPrincipalName",
      "scoped" : true,
      "script": "groovy { logger.info(\" name: ${attributeName}, values: ${attributeValues} \"); return ['Hi', attributes['firstname']] }"
    }
}
```  

If the CAS server has a scope of `example.org`,
the final values of `eduPersonPrincipalName` would be [`Hi, casuser`]
released as `urn:oid:1.3.6.1.4.1.5923.1.1.1.6` with a friendly name of `eduPersonPrincipalName`.
{% endtab %}

{% tab attrdefinitions <i class="fa fa-file-code px-1"></i>External Script %}
Same use case as above, except the attribute value be additionally processed by an external Groovy script:

```json 
{
    "@class" : "java.util.TreeMap",
    "eduPersonPrincipalName" : {
      "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
      "key" : "eduPersonPrincipalName",
      "name" : "urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
      "friendlyName" : "eduPersonPrincipalName",
      "scoped" : true,
      "script": "file:/attribute-definitions.groovy"
    }
}
```  

The outline of the Groovy script should be defined as:

```groovy
def run(Object[] args) {
    def (attributeName,attributeValues,logger,registeredService,attributes) = args
    logger.info("name: ${attributeName}, values: ${attributeValues}, attributes: ${attributes}")
    return ["Hello " + attributes['givenName']]
}
```

If the CAS server has a scope of `example.org`,
the final values of `eduPersonPrincipalName` would be [`Hello casuser`]
released as `urn:oid:1.3.6.1.4.1.5923.1.1.1.6` with a friendly name of `eduPersonPrincipalName`.

{% endtab %}

{% tab attrdefinitions Regular Expression Patterns %}

Define an attribute definition to produce values conditionally based on pattern matching rules.
If the attribute definition is to build its values off of an existing resolved attribute,
each available value is examined against patterns defined here in the `patterns` map. For each match, the linked entry
is used to determine the attribute definition value, either statically or dynamically which is typically an inlined Groovy script.
If a pattern match is not found, then the value is skipped.

```json 
{
    "@class" : "java.util.TreeMap",
    "memberships" : {
        "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
        "key": "memberships",
        "attribute" : "memberships",
        "name": "affiliations",
        "patterns" : {
          "@class" : "java.util.TreeMap",
          "m[0-2].*" : "admins",
          "m[3-6].*" : "groovy { return 'users' }"
        }
    }
}
```
     
The above snippet builds an attribute definition, `memberships`, that is ultimately encoded and released
to applications under the name `affiliations`. The attribute values for this definition are sourced from 
the `memberships` attribute that must be made available to CAS. Each value is examined against the patterns
defined and on a successful match, the linked construct will be evaluated to determine the final value.

For example, if the resolved set of attributes are `memberships=[m1, m2, m3, m4, m9]`,
the final values of `memberships` would be [`admins`,`users`] which would then be released under the name `affiliations`.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab attrdefinitions Flattened %}
   
If the collection of attributes values assembled for the attribute definition ultimately contain more than one value,
the attribute definition may be instructed to flatten all values using the given delimiter character into a single value.

```json 
{
    "@class" : "java.util.TreeMap",
    "allgroups" : {
        "@class" : "org.apereo.cas.authentication.attribute.DefaultAttributeDefinition",
        "key": "allgroups",
        "attribute" : "memberships",
        "flattened": "/"
    }
}
```  

For example, if the resolved set of attributes are `memberships=[m1, m2, m3, m4, m9]`,
the final values of `memberships` would be `m1/m2/m3/m4`.

{% endtab %}

{% endtabs %}
    
## Custom Attribute Definitions

You may design and inject your own attribute definitions dynamically with CAS. First, you will need to design
a `@AutoConfiguration` class to contain your own `AttributeDefinitionStoreConfigurer` implementation:

```java
@AutoConfiguration
public class MyConfiguration {

    @Bean
    public AttributeDefinitionStoreConfigurer myAttributeDefinitionStore() {
        ...
    }
}
```

Your configuration class needs to be registered with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.
