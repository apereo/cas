---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# Activation - Attribute Consent

Attribute consent activation can be controlled both at a global and per-service level. By default, global activation rules for 
attribute consent are turned on and the consent policy rules for the service definition are disabled. The consent policy for
the service definition may override the global rules using the `status` field which accepts the following values:

| Value       | Description                                                                       |
|-------------|-----------------------------------------------------------------------------------|
| `FALSE`     | Consent policy is disabled, overriding the global configuration.                  |
| `TRUE`      | Consent policy is enabled, overriding the global configuration.                   |
| `UNDEFINED` | Consent policy is undefined, delegating the decision to the global configuration. |

Note that attribute consent policies may also be chained together to compose multiple policies. Each policy 
can be individually disabled or enabled and the overall aggregate status
of the entire attribute consent policy will be used to determine attribute consent activation and 
selection. A sample chain of attribute consent polices follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [ "java.util.ArrayList",
      [
        {
          "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
          "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
          "consentPolicy": {
            "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
            "includeOnlyAttributes": ["java.util.LinkedHashSet", ["cn"]],
            "status": "TRUE"
          }
        },
        {
          "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
          "allowedAttributes" : [ "java.util.ArrayList", [ "displayName" ] ],
          "consentPolicy": {
            "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
            "includeOnlyAttributes": ["java.util.LinkedHashSet", ["displayName"]],
            "status": "FALSE"
          }
        }
      ]
    ]
  }
}
```          

## Activation via Groovy

The default consent activation strategy can be replaced with an external Groovy script to determine whether the request 
qualifies for consent. Path to the script is defined via CAS configuration properties.

{% include_cached casproperties.html properties="cas.consent.activation-strategy-groovy-script" %}

The script itself may be designed as such:

```groovy
import org.apereo.cas.util.model.TriStateBoolean

def run(Object[] args) {
    def consentEngine = args[0]
    def casProperties = args[1]
    def service = args[2]
    def registeredService = args[3]
    def authentication = args[4]
    def request = args[5]
    def logger = args[6]

    logger.debug("Activating consent for ${registeredService.name}")
    return true;
}
```

The following parameters are passed to the script:

| Parameter             | Description
|-----------------------------------------------------------------------------------------------------------------
| `consentEngine`       | A reference to the `ConsentEngine` object.
| `casProperties`       | A reference to the CAS configuration properties loaded from property sources.
| `service`             | The `Service` object representing the requesting application.
| `registeredService`   | The `RegisteredService` object representing the service definition in the registry.
| `authentication`      | The `Authentication` object representing the active authentication transaction.
| `request`             | The object representing the HTTP servlet request.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.

The script is expected to return either `true` or `false` to determine whether or not consent is required.
