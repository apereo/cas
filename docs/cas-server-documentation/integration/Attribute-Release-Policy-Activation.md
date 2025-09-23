---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Activation Criteria

Almost all attribute release policies can be modified to conditionally activate the release policy. This allows the
release policy to dynamically decide whether attributes should be released at all to the target application.

You can implement the conditions using the following strategies.
        
{% tabs attrreleaseconditions %}

{% tab attrreleaseconditions Attributes <i class="fa fa-id-card px-1"></i> %}

The release policy can be conditionally activated based on available principal attributes.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://app.example.org",
  "name" : "Example",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
    "activationCriteria": {
        "@class": "org.apereo.cas.services.AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria",
        "operator": "AND",
        "reverseMatch": false,
        "requiredAttributes": {
          "@class" : "java.util.HashMap",
          "firstName": [ "java.util.ArrayList", [ "John", "Jon" ] ],
          "lastName": [ "java.util.ArrayList", [ "Holdoor", "Hodor" ] ]
        }
    }
  }
}
```

In the above example, the attribute release policy is only activated if the current 
principal has a `lastName` attribute with values `Jon` or `John`, AND has a `firstName` attribute 
with values `Holdoor` or `Hodor`. The operator field can also be modified to use `OR`.
    
The `reverseMatch` field can be used to invert the logic of the condition. When set to `true`,   
principal must not carry any of the required attributes for the policy to activate.

{% endtab %}

{% tab attrreleaseconditions <i class="fa fa-code px-1"></i>Groovy %}

The release policy can be conditionally activated via a Groovy script, that may be defined either inline
or outsourced to an external Groovy script.

Below shows the option where you define an external Groovy script:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
    "activationCriteria": {
        "@class":"org.apereo.cas.services.GroovyRegisteredServiceAttributeReleaseActivationCriteria",
        "groovyScript" : "file:///path/to/script.groovy"
    }
  }
}
```

The script itself may be designed as:

```groovy
def run(Object[] args) {
    def (context,logger) = args
    def principal = context.principal
    logger.info("Principal id is ${principal.id}, service is ${context.service}")
    if (principal.id == 'Gandalf') {
        logger.info("User is too powerful; Releasing attributes is allowed")
        return true
    }
    return false
}
```

The following parameters are passed to the script:

| Parameter | Description                                                                 |
|-----------|-----------------------------------------------------------------------------|
| `context` | The object that carries the attribute release execution context.            |
| `logger`  | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

You may also do the same sort of thing with an inline Groovy script:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
    "activationCriteria": {
        "@class":"org.apereo.cas.services.GroovyRegisteredServiceAttributeReleaseActivationCriteria",
        "groovyScript" : "groovy { context.principal.id == 'Gandalf' }"
    }
  }
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab attrreleaseconditions <i class="fa fa-link px-1"></i>Chaining %}

You can also combine multiple activation criteria using a chaining setup:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
    "activationCriteria": {
        "@class":"org.apereo.cas.services.ChainingRegisteredServiceAttributeReleaseActivationCriteria",
        "conditions": [ "java.util.ArrayList", [
            {
                "@class": "org.apereo.cas.services.AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria",
                "operator": "AND",
                "requiredAttributes": {
                  "@class": "java.util.HashMap",
                  "cn": [ "java.util.ArrayList", [ "confidential" ] ]
                }
            },
            {
                "@class": "org.apereo.cas.services.GroovyRegisteredServiceAttributeReleaseActivationCriteria",
                "groovyScript": "groovy { return ... }"
            }
        ]],
        "operator": "AND"
    }
  }
}
```

{% endtab %}

{% endtabs %}


