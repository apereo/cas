---
layout: default
title: CAS - SAML2 Delegated Authentication
category: Authentication
---

{% include variables.html %}

# SAML2 Delegated Authentication - JDBC Service Provider Metadata

SAML2 metadata for CAS as the SAML2 service provider may also be managed inside a relational database instance. To active this feature, you need to start by 
including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-util" %}

{% include_cached featuretoggles.html features="DelegatedAuthentication.saml-jdbc" %}

Finally, please make sure you have specified a table name in your CAS settings that would ultimately house the generated SAML2 metadata.

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata.service-provider" includes="jdbc" %}

The table structure and schema needs to be created and must exist prior to CAS starting up, and it should be modeled after the following SQL statement:

```sql
CREATE TABLE <table-name> (entityId VARCHAR(512), metadata TEXT)
```
