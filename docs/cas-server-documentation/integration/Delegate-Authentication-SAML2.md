---
layout: default
title: CAS - SAML2 Delegated Authentication
category: Authentication
---

{% include variables.html %}

# SAML2 Delegated Authentication

CAS can be configured to route the authentication requests to one or more external SAML2 identity providers. 
Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-saml" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.saml" excludes=".metadata" %}

## Metadata Management

Please [see this guide](Delegate-Authentication-SAML2-Metadata.html).     

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

Please [see this guide](Delegate-Authentication-SAML2-Discovery.html).

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.pac4j" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>

<Logger name="org.opensaml" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>

<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```


