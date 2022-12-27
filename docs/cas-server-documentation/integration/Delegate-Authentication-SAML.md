---
layout: default
title: CAS - Delegate Authentication w/ SAML2 Identity Providers
category: Authentication
---

{% include variables.html %}

# Delegated Authentication w/ SAML2

In the event that CAS is configured to delegate authentication to an external identity provider, the service provider (CAS) 
metadata as well as the identity provider metadata automatically become available at the following endpoints:

| Endpoint                        | Description                                                                                                                                                                 |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/sp/metadata`                  | Displays the service provider (CAS) metadata. Works well if there is only one SAML2 IdP is defined.                                                                         |
| `/sp/idp/metadata`              | Displays the identity provider metadata. Works well if there is only one SAML2 IdP is defined. Accepts a `force=true` parameter to reload the identity provider's metadata. |
| `/sp/{clientName}/metadata`     | Displays the service provider metadata for the requested client name.                                                                                                       |
| `/sp/{clientName}/idp/metadata` | Displays the identity provider metadata for the requested client name. Accepts a `force=true` parameter to reload the identity provider's metadata                          |

Note that you can use more than one external identity provider with CAS, where each integration may be done 
with a different set of metadata and keys for CAS acting as the service provider. Each integration (referred to as a client, 
since CAS itself becomes a client of the identity provider) may be given a name optionally.

Remember that the service provider (CAS) metadata is automatically generated once you access the above 
endpoints or view the CAS login screen. This is required because today, generating the metadata requires 
access to the HTTP request/response. In the event that metadata cannot 
be resolved, a status code of `406 - Not Acceptable` is returned.

{% include_cached casproperties.html properties="cas.authn.pac4j.saml" excludes=".metadata" %}

## Metadata Management

SAML2 metadata for both the delegated identity provider as well as the (CAS) service provider can managed via the following settings.

{% tabs pac4jsaml2md %}

{% tab pac4jsaml2md Identity Provider %}

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata" includes=".identity" id="pac4jsaml2metadata" %}

{% endtab %}

{% tab pac4jsaml2md Service Provider %}

### Default

SAML2 metadata for CAS as the SAML2 service provider is typically managed on disk, and generated on startup if the metadata file
is not found. Future and subsequent changes to this metadata file, if necessary, must be handled manually and the file might
need to be curated and edited to fit your purposes.

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata.service-provider"  includes=".file-system" %}

### MongoDb

SAML2 metadata for CAS as the SAML2 service provider may also be managed inside a MongoDb instance. To active this feature, you need to start by including 
the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mongo-core" %}

Next, you should activate and turn on the feature:

{% include_cached featuretoggles.html features="DelegatedAuthentication.saml-mongodb" %}
                                                                                    
Finally, please make sure you have specified a collection name in your CAS settings that would ultimately house the generated SAML2 metadata.

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata.service-provider" includes="mongo" %}

### JDBC

SAML2 metadata for CAS as the SAML2 service provider may also be managed inside a relational database instance. To active this feature, you need to start by 
including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-util" %}

Next, you should activate and turn on the feature:

{% include_cached featuretoggles.html features="DelegatedAuthentication.saml-jdbc" %}

Finally, please make sure you have specified a table name in your CAS settings that would ultimately house the generated SAML2 metadata.

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata.service-provider" includes="jdbc" %}

The table structure and schema needs to be created and must exist prior to CAS starting up, and it should be modeled after the following SQL statement:

```sql
CREATE TABLE <table-name> (entityId VARCHAR(512), metadata TEXT)
```

{% endtab %}

{% endtabs %}

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

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.opensaml" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>

<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```

## Identity Provider Discovery Service

<div class="alert alert-info"><strong>Note</strong><p>Using identity provider discovery requires 
delegated authentication to be available. This feature cannot be used on its own
as a standalone discovery service.</p></div>

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-discovery" %}

Identity provider discovery allows CAS 
to [embed and present a discovery service](https://wiki.shibboleth.net/confluence/display/EDS10/Embedded+Discovery+Service) 
as part of delegated authentication. Configured SAML2 identity providers in the CAS configuration
used for delegated authentication are presented as options for discovery. 

CAS is also able to directly consume multiple JSON feeds
that contain discovery metadata about available identity providers. The discovery JSON feed 
may be fetched from a URL (i.e. exposed by a Shibboleth Service Provider) or it may
directly be consumed as a JSON file with the following structure:

```json
[{
 "entityID": "https://idp.example.net/idp/saml",
 "DisplayNames": [{
  "value": "Example.net",
  "lang": "en"
  }],
 "Descriptions": [{
  "value": "An identity provider for the people, by the people.",
  "lang": "en"
  }],
 "Logos": [{
  "value": "https://example.net/images/logo.png",
  "height": "90",
  "width": "62"
  }]
}]
```

The following endpoints are available:

| Endpoint                  | Description                                                              |
|---------------------------|--------------------------------------------------------------------------|
| `/idp/discovery`          | Identity provider discovery landing page.                                |
| `/idp/discovery/feed`     | Identity provider discovery JSON feed.                                   |
| `/idp/discovery/redirect` | Return endpoint to let CAS invoke the identity provider after selection. |

Applications may directly invoke the discovery service via `[cas-server-prefix]/idp/discovery`. The discovery service may also 
be invoked using the discovery protocol via `[cas-server-prefix]/idp/discovery?entityID=[service-provider-entity-id]&return=[cas-server-prefix]/idp/discovery/redirect`. 
Additional parameters may be included as part of the `return` url and they all must be encoded.
