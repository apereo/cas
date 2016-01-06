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

{% highlight xml %}
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-support-ldap-service-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
{% endhighlight %}


## Configuration

{% highlight xml %}
...
<alias name="ldapServiceRegistryDao" alias="serviceRegistryDao" />
<alias name="mySearchRequest" alias="ldapServiceRegistrySearchRequest" />
<alias name="myConnectionFactory" alias="ldapServiceRegistryConnectionFactory" />
...
{% endhighlight %}

The default mapper has support for the following optional items:

| Field                             | Default Value
|-----------------------------------+--------------------------------------------------+
| `objectClass`                     | casRegisteredService
| `serviceDefinitionAttribute`      | description
| `idAttribute`                     | uid

Service definitions are by default stored inside the `serviceDefinitionAttribute` attribute as
JSON objects. The format and syntax of the JSON is identical to that of
[JSON Service Registry](JSON-Service-Management.html).
