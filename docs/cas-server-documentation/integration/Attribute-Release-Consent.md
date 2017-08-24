---
layout: default
title: CAS - Attribute Release Consent
---

# Attribute Consent

CAS provides the ability to enforce user-informed consent upon attribute release. Practically, this means that prior to accessing the target application, the user will be presented with a collection of attributes allowed to be released to the application with options to either proceed or deny the release of said attributes. There are also additional options to indicate how should underlying changes in the attribute release policy be considered by the consent engine. Users are also provied the ability to set up reminders in the event that no change is detected in the attribute release policy.

Consent attribute records stored in the configured repository are signed and encrypted.

Support is enabled by including the following module in the Overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-consent-webflow</artifactId>
     <version>${cas.version}</version>
</dependency>
```

## Attribute Selection

By default, all attributes that are marked for release do qualify for consent. To control this process, you may define a consent policy that indicates a criteria by which attribute selection for consent is carried out.

The policy assigned to each service includes the following features:

| Field                      | Description
|----------------------------|---------------------------------------------------------------------------------------
| `excludedAttributes`       | Exclude the indicated attributes from consent.
| `includeOnlyAttributes`    | Force-include the indicated attributes in consent, provided attributes are resolved.
| `enabled`                  | Control whether consent is active/inactive for this service. Default is `true`.

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
      "excludedAttributes":["java.util.LinkedHashSet", ["test"]],
      "includeOnlyAttributes":["java.util.LinkedHashSet", ["test"]],
      "enabled": true
    }
  }
}
```

## Storage

User consent decisions may be stored and remembered using one of the following options.

### JSON

This is the default option, most useful for demo and testing purposes. Consent decisions are all
kept inside a static JSON resource whose path is taught to CAS via settings.

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html#json-attribute-consent).

### JDBC

Support is enabled by including the following module in the Overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-consent-jdbc</artifactId>
     <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html#jpa-attribute-consent).


### REST

Support is enabled by including the following module in the Overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-consent-rest</artifactId>
     <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html#rest-attribute-consent).

Endpoints must be designed to accept/process `application/json`.

| Operation                 | Method    | Data                                 | Expected Response
|---------------------------|-----------|--------------------------------------------------------------------------------------
| Locate consent decision   | `GET`     | `service`, `principal` as headers    | `200`. The consent decision object in the body.
| Store consent decision    | `POST`    |  Consent decision object in the body | `200`.

The consent decision object in transit will and must match the following structure:

```json
{
   "id": 1000,
   "principal": "casuser",
   "service": "https://google.com",
   "date": [ 2017, 7, 10, 14, 10, 17 ],
   "options": "ATTRIBUTE_NAME",
   "reminder": 14,
   "reminderTimeUnit": "DAYS",
   "attributeNames": "...",
   "attributeValues": "..."
}
```

| Field                     | Description    
|---------------------------|-----------------------------------------------------------------------------------------------------------------------
| `id`                      | `-1` for new decision records or a valid numeric value for existing records.      
| `principal`               | The authenticated user id.
| `service`                 | Target application url to which attributes are about to be released.
| `date`                    | Date/Time of the decision record.
| `options`                 | Indicates how changes in attributes are determined for this application. (i.e. `ATTRIBUTE_NAME`, `ATTRIBUTE_VALUE`, `ALWAYS`)
| `reminder`                | Indicates the period after which user will be reminded to consent again, in case no changes are found.
| `reminderTimeUnit`        | The reminder time unit (i.e. `MONTHS`, `DAYS`, `HOURS`, etc).
| `attributeNames`          | SHA-512 of attribute names for this application, signed and encrypted.
| `attributeValues`         | SHA-512 of attribute values for this application, signed and encrypted.


### Custom

You may also inject your own implementation for attribute consent management into CAS that would itself handle storing consent decisions, etc. In order to do this, you will need to design a configuration class that roughly matches the following: 

```java
package org.apereo.cas.pm;

@Configuration("MyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    public ConsentRepository consentRepository() {
        ...
    }
}
```

[See this guide](../installation/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

