---
layout: default
title: CAS - Delegate Authentication w/ SAML2 Identity Providers
category: Authentication
---

{% include variables.html %}

# SAML2 Delegated Authentication - Metadata Management

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

## Strategies

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
