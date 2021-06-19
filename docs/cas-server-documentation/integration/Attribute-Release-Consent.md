---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# Attribute Consent

CAS provides the ability to enforce user-informed consent upon attribute release. Practically, this 
means that prior to accessing the target application, the
 user will be presented with a collection of attributes allowed to be released to the application with 
options to either proceed or deny the release of said attributes. There are also additional options to 
indicate how should underlying changes in the attribute release policy be considered by the consent 
engine. Users are also provided the ability to set up reminders in the event that no change is detected in the attribute release policy.

Consent attribute records stored in the configured repository are signed and encrypted.

Support is enabled by including the following module in the WAR Overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-consent-webflow" %}

## Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                 | Description
|--------------------------|------------------------------------------------
| `attributeConsent`       | Manage and control [attribute consent decisions](Attribute-Release-Consent.html). A `GET` operation produces a list of all consent decisions. A `DELETE` operation with a record key id will attempt to remove and revoke the registered device (i.e. `attributeConsent/{principal}/{id}`).
| `attributeConsent/export`       | Invoked via `GET` to export all consent decisions as a downloadable zip archive.
| `attributeConsent/import`       | Invoked via `POST` to import a single consent decision provided in the request body.

## Attribute Selection

By default, all attributes that are marked for release do qualify for consent. To control this process, you 
may define a consent policy that indicates a criteria by which attribute selection for consent is carried out.

The policy assigned to each service includes the following features:

| Field                      | Description
|----------------------------|---------------------------------------------------------------------------------------
| `excludedAttributes`       | Exclude the indicated attributes from consent.
| `includeOnlyAttributes`    | Force-include the indicated attributes in consent, provided attributes are resolved.
| `status`                   | Controls whether consent for this service should be activated. See below for activation rules.

A sample definition follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllAttributeReleasePolicy",
    "consentPolicy": {
      "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
      "excludedAttributes": ["java.util.LinkedHashSet", ["test"]],
      "includeOnlyAttributes": ["java.util.LinkedHashSet", ["test"]],
      "status": "FALSE"
    }
  }
}
```

## Activation

Attribute consent activation can be controlled both at a global and per-service level. By default, global activation rules for 
attribute consent are turned on and the consent policy rules for the service definition are disabled. The consent policy for
the service definition may override the global rules using the `status` field which accepts the following values:

| Value       | Description
|-------------|---------------------------------------------------------------------------------------
| `FALSE`     | Consent policy is disabled, overriding the global configuration.
| `TRUE`      | Consent policy is enabled, overriding the global configuration.
| `UNDEFINED` | Consent policy is undefined, delegating the decision to the global configuration.

Note that attribute consent policies may also be chained together to compose multiple policies. Each policy 
can be individually disabled or enabled and the overall aggregate status
of the entire attribute consent policy will be used to determine attribute consent activation and 
selection. A sample chain of attribute consent polices follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
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

### Activation via Groovy

The default consent activation strategy can be replaced with an external Groovy script to determine whether the request 
qualifies for consent. Path to the script is defined via CAS configuration properties.

{% include casproperties.html properties="cas.consent.activation-strategy-groovy-script" %}

The script itself may be designed as such:

```groovy
import org.apereo.cas.util.model.TriStateBoolean

def run(Object[] args) {
    def consentEngine = args[0]
    def casProperties = args[1]
    def service = args[2]
    def registeredService = args[3]
    def authentication = args[4]
    def requestContext = args[5]
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
| `requestContext`      | The object representing the Spring Webflow `RequestContext`.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.

The script is expected to return either `true` or `false` to determine whether or not consent is required.

## Storage

User consent decisions may be stored and remembered using one of the following options.

| Storage          | Description                                         
|--------------------------------------------------------------------------------------
| CoucbDb          | [See this guide](Attribute-Release-Consent-Storage-CouchDb.html).
| Custom           | [See this guide](Attribute-Release-Consent-Storage-Custom.html).
| Groovy           | [See this guide](Attribute-Release-Consent-Storage-Groovy.html).
| JDBC             | [See this guide](Attribute-Release-Consent-Storage-JDBC.html).
| JSON             | [See this guide](Attribute-Release-Consent-Storage-JSON.html).
| LDAP             | [See this guide](Attribute-Release-Consent-Storage-LDAP.html).
| MongoDb          | [See this guide](Attribute-Release-Consent-Storage-MongoDb.html).
| Redis            | [See this guide](Attribute-Release-Consent-Storage-Redis.html).
| REST             | [See this guide](Attribute-Release-Consent-Storage-REST.html).
