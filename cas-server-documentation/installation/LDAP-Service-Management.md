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

{% highlight xml %}

<context:component-scan base-package="org.jasig.cas" />

<bean id="serviceRegistryDao"
      class="org.jasig.cas.adaptors.ldap.services.LdapServiceRegistryDao"
      p:connectionFactory-ref="pooledLdapConnectionFactory"
      p:searchRequest-ref="searchRequest"
      p:ldapServiceMapper-ref="ldapServiceMapper" />

<bean id="ldapServiceMapper"
      class="org.jasig.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapper"/>
{% endhighlight %}

Note that the configuration of the mapper is optional and need not explicitly exist.

<p/>

######`DefaultLdapRegisteredServiceMapper`
The default mapper has support for the following optional items:

| Field                             | Default Value
|-----------------------------------+--------------------------------------------------+
| `objectClass`                     | casRegisteredService
| `serviceDefinitionAttribute`      | description
| `idAttribute`                     | uid

Service definitions are by default stored inside the `serviceDefinitionAttribute` attribute as 
JSON objects. The format and syntax of the JSON is identical to that of 
[JSON Service Registry](JSON-Service-Management.html).
