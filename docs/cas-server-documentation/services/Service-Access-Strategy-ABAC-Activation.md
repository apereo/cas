---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - ABAC Activation Criteria
                                                                 
The ABAC strategy can be modified to conditionally activate the access strategy policy, and then optionally decide
the final access strategy result if the strategy is determined to remain inactive. 

You can implement the conditions using the following strategies.

{% tabs abacconditions %}

{% tab abacconditions Attributes %}
The activation strategy can be conditionally activated based on available principal attributes.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://app.example.org",
  "name" : "Example",
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ]
    },
    "activationCriteria": {
       "@class": "org.apereo.cas.services.AttributeBasedRegisteredServiceAccessStrategyActivationCriteria",
        "allowIfInactive": false,
        "operator": "AND",
        "requiredAttributes": {
          "@class" : "java.util.HashMap",
          "firstName": [ "java.util.ArrayList", [ "John", "Jon" ] ],
          "lastName": [ "java.util.ArrayList", [ "Holdoor", "Hodor" ] ]
        }
    }
  }
}
```

In the above example, the access strategy is only activated if the current principal

- has a `lastName` attribute with values `Jon` or `John`
- **...AND...**  (This is controlled by the `operator` field, which you can also alter to use `OR`)
- has a `firstName` attribute with values `Holdoor` or `Hodor`

Note that if the access strategy fails to activate and must remain inactive, then access is denied via `allowIfInactive`.
{% endtab %}

{% tab abacconditions Groovy %}
You can decide whether the access strategy should be activated using a Groovy script, that may be defined either inline
or outsourced to an external Groovy script.

Below shows the option where you define the Groovy script directly within the body of the service definition:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://app.example.org",
  "name" : "Example",
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ]
    },
    "activationCriteria": {
       "@class": "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyActivationCriteria",
       "groovyScript": "groovy { return accessRequest.principalId.startsWith('admin-') }",
       "allowIfInactive": true
    }
  }
}
```

The `accessRequest` object allows one to define conditions based on the following attached properties:

| Code                | Description                                                                              |
|---------------------|------------------------------------------------------------------------------------------|
| `principalId`       | The identifier of the current principal after a successful authentication event.         |
| `attributes`        | A `Map` of attributes attached to the principal after a successful authentication event. |
| `service`           | The `Service` object representing the current application request.                       |
| `registeredService` | The registered service definition in CAS that is mapped to the `Service`.                |

Alternatively, you could also outsource the Groovy script to an external file:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://app.example.org",
  "name" : "Example",
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ]
    },
    "activationCriteria": {
       "@class": "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyActivationCriteria",
       "groovyScript": "file:///etc/cas/config/AccessStrategy.groovy",
       "allowIfInactive": true
    }
  }
}
```

The script itself may be designed as:

```groovy
import org.apereo.cas.services.*

def run(Object[] args) {
    def context = args[0] as RegisteredServiceAccessStrategyRequest
    def logger = args[1]
    logger.info("Checking ${context.principalId} - ${context.registeredService.name}")
    return true
}
```
{% endtab %}

{% tab abacconditions Chaining %}
You can also combine multiple activation criteria using a chaining setup:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://app.example.org",
  "name" : "Example",
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ]
    },
    "activationCriteria": {
        "@class": "org.apereo.cas.services.ChainingRegisteredServiceAccessStrategyActivationCriteria",
        "conditions": [ "java.util.ArrayList", [
          {
            "@class": "org.apereo.cas.services.AttributeBasedRegisteredServiceAccessStrategyActivationCriteria",
            "order": 1,
            "requiredAttributes": {
              "@class": "java.util.HashMap",
              "cn": [ "java.util.ArrayList", [ "name1", "name2" ]]
            }
          },
          {
            "@class": "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyActivationCriteria",
            "order": 2,
            "groovyScript": "groovy { return accessRequest.principalId.startsWith('admin-') }"
          }
        ]],
        "operator": "AND"
    }
  }
}
```

{% endtab %}

{% endtabs %}
