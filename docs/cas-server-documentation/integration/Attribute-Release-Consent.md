---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# Attribute Consent

CAS provides the ability to enforce user-informed consent upon attribute release. Practically, this 
means that prior to accessing the target application, the user will be presented with a 
collection of attributes allowed to be released to the application with 
options to either proceed or deny the release of said attributes. There are also additional options to 
indicate how should underlying changes in the attribute release policy be considered by the consent 
engine. Users are also provided the ability to set up reminders in the event 
that no change is detected in the attribute release policy.

Consent attribute records stored in the configured repository are signed and encrypted.

Support is enabled by including the following module in the WAR Overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-consent-webflow" %}

## Configuration

{% include casproperties.html properties="cas.consent.enabled,cas.consent.active,cas.consent.reminder,cas.consent.reminder-time-unit" %}

## Actuator Endpoints
      
The following endpoints are provided by CAS:

{% include actuators.html endpoints="attributeConsent" %}

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

See [this guide](Attribute-Release-Consent-Activation.html) for more details.

## Storage

User consent decisions may be stored and remembered using one of the following options.

| Storage          | Description                                         
|--------------------------------------------------------------------------------------
| CouchDb          | [See this guide](Attribute-Release-Consent-Storage-CouchDb.html).
| Custom           | [See this guide](Attribute-Release-Consent-Storage-Custom.html).
| Groovy           | [See this guide](Attribute-Release-Consent-Storage-Groovy.html).
| JDBC             | [See this guide](Attribute-Release-Consent-Storage-JDBC.html).
| JSON             | [See this guide](Attribute-Release-Consent-Storage-JSON.html).
| LDAP             | [See this guide](Attribute-Release-Consent-Storage-LDAP.html).
| MongoDb          | [See this guide](Attribute-Release-Consent-Storage-MongoDb.html).
| Redis            | [See this guide](Attribute-Release-Consent-Storage-Redis.html).
| REST             | [See this guide](Attribute-Release-Consent-Storage-REST.html).
