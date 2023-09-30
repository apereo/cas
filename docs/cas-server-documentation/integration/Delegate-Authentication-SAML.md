---
layout: default
title: CAS - Delegate Authentication w/ SAML2 Identity Providers
category: Authentication
---

{% include variables.html %}

# SAML2 Delegated Authentication

{% include_cached casproperties.html properties="cas.authn.pac4j.saml" excludes=".metadata" %}

## Metadata Management

Please [see this guide](Delegate-Authentication-SAML-Metadata.html).     

## Per Service Customizations

Th configuration for the external SAML2 identity provider is typically done at build time
via CAS configuration settings and applies to all applications and relying parties. You may override
certain aspects this configuration on a per application basis by assigning 
dedicated [properties to the service definition](../services/Configuring-Service-Custom-Properties.html).

{% include_cached registeredserviceproperties.html groups="DELEGATED_AUTHN,DELEGATED_AUTHN_SAML2" %}

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
  "name" : "Example",
  "id" : 1,
  "properties" : {
    "@class" : "java.util.HashMap",
    "AuthnContextClassRefs" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "https://refeds.org/profile/mfa" ] ]
    },
    "WantsAssertionsSigned" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "false" ] ]
    }
  }
}
```
       
See [registered service properties](../services/Configuring-Service-Custom-Properties.html) for more details.

## Identity Provider Discovery Service

Please [see this guide](Delegate-Authentication-SAML-Discovery.html).

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.opensaml" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>

<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```


