---
layout: default
title: CAS - LDAP Service Registry
---

# LDAP Service Registry

Service registry implementation which stores the services in a LDAP Directory.
Uses an instance of `LdapRegisteredServiceMapper`, that by default is `DefaultLdapRegisteredServiceMapper`
in order to configure settings for retrieval, search and persistence of service definitions.
By default, entries are assigned the `objectclass` `casRegisteredService`
attribute and are looked up by the `uid` attribute.

Support is enabled by adding the following module into the Maven overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-ldap-service-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```

## Configuration

The default mapper has support for the following optional items:

| Field                             | Default Value
|-----------------------------------|---------------------------------------------------
| `objectClass`                     | casRegisteredService
| `serviceDefinitionAttribute`      | description
| `idAttribute`                     | uid

Service definitions are by default stored inside the `serviceDefinitionAttribute` attribute as
JSON objects. The format and syntax of the JSON is identical to that of
[JSON Service Registry](JSON-Service-Management.html).

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#mongodb-service-registry).

## Auto Initialization

Upon startup and configuration permitting,
the registry is able to auto initialize itself from default
JSON service definitions available to CAS.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#service-registry).
