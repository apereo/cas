---
layout: default
title: CAS - Multitenancy - Tenant Properties
description: "Give a tenant its own CAS settings, such as an email server, authentication handlers or attribute repositories, with the properties map of its definition."
category: Multitenancy
---

{% include variables.html %}

# Tenant Properties - Multitenancy

The `properties` field of a [tenant definition](Multitenancy-Tenant-Capabilities.html) holds CAS settings that apply only to that tenant, such as its own email server, authentication handlers or attribute repositories.

The tenant properties field is a map of CAS properties that are effective for this tenant. CAS features
and modules that do support multitenancy are able to read this map and apply the properties
to the tenant context. Examples here may include defining email server settings, authentication handler
construction and more.

Tenant definition properties may be defined and secured via [CAS configuration security](../configuration/Configuration-Properties-Security-CAS.html).

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
Not every CAS configuration property is multitenant-aware, and this capability is 
limited to CAS features and modules that are explicitly designed to support 
multitenancy. Support for multitenancy is evolving and new features and support for more modules may be added in future releases.
Please check the documentation for each feature or module to see if it supports multitenancy.
</p></div>

The following examples are available:

{% tabs multitenancyexamples %}

{% tab multitenancyexamples Email Server %}

The following tenant definition is allowed to define its [email server](../notifications/Sending-Email-Configuration.html):

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "spring.mail.host": "localhost",
        "spring.mail.port": 25000
      }
    }
  ]
]
```

{% endtab %}

{% tab multitenancyexamples LDAP Authentication %}

The following tenant definition is allowed to define its [LDAP authentication](../authentication/LDAP-Authentication.html):

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.authn.ldap[0].type": "DIRECT",
        "cas.authn.ldap[0].dn-format": "uid=%s,ou=people,dc=example,dc=org",
        "cas.authn.ldap[0].ldap-url": "ldap://localhost:11389"
      }
    }
  ]
]
```

{% endtab %}

{% tab multitenancyexamples JDBC Authentication %}

The following tenant definition is allowed to define its [JDBC authentication](../authentication/Database-Authentication.html):

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.authn.jdbc.procedure[0].procedure-name": "sp_authenticate",
        "cas.authn.jdbc.procedure[0].user": "postgres",
        "cas.authn.jdbc.procedure[0].password": "...",
        "cas.authn.jdbc.procedure[0].driver-class": "org.postgresql.Driver",
        "cas.authn.jdbc.procedure[0].url": "jdbc:postgresql://localhost:5432/users",
        "cas.authn.jdbc.procedure[0].dialect": "org.hibernate.dialect.PostgreSQLDialect"
      }
    }
  ]
]
```

{% endtab %}

{% tab multitenancyexamples Delegated Authentication %}

The following tenant definition is allowed to define its [external identity provider](../integration/Delegate-Authentication.html):

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.authn.pac4j.cas[0].login-url": "https://sso.example.org/cas/login"
      }
    }
  ]
]
```

{% endtab %}

{% tab multitenancyexamples Multifactor Authentication %}

The following tenant definition will activate 
[Multifactor Authentication](../mfa/Configuring-Multifactor-Authentication-Triggers.html)
based on [Duo Security](../mfa/DuoSecurity-Authentication.html):

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.authn.mfa.triggers.global.global-provider-id": "mfa-duo"
      }
    }
  ]
]
```

{% endtab %}

{% tab multitenancyexamples Configuration Security %}
      
Configuration properties assigned to a tenant definition may be secured via
[CAS configuration security](../configuration/Configuration-Properties-Security-CAS.html):

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "description": "Shire tenant",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.some.property": "{cas-cipher}xoLrkVhqnyAmMHqxWi3t+AcXf/w6Mg3bltpdP1kmG9E="
      }
    }
  ]
]
```

{% endtab %}

{% tab multitenancyexamples Virtual Hosts & Routing %}

CAS employs a special filter that is able to map an incoming request to a tenant definition based on the `Host`
header that is ultimately picked up by `HttpServletRequest#getServerName()`. A matching request will be routed
to the appropriate tenant url.
       
- If the `Host` header is given as `sso.example.org`, i.e. via a reverse proxy, the `shire` tenant definition will allow
CAS to route requests from `https://sso.example.org/cas/login` to `https://${cas.server.domain}/cas/tenants/shire/login`.

- If the `Host` header is given as `sso.example.com`, the `london` tenant definition will allow
CAS to route requests from `https://sso.example.org/cas/login` to `https://${cas.server.domain}/cas/tenants/london/login`.

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.server.name": "https://sso.example.org"
      }
    },
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "london",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.host.name": "sso.example.com"
      }
    }
  ]
]
```
   
This setup is useful in scenarios where there is a reverse proxy that sits in front of CAS
and is able to route traffic for predefined hosts to CAS tenants. For example, the setup below
for nginx, combined with the tenant definitions, allows CAS to route traffic for `sso.example.com` to 
the `london` tenant definition that carries 
its own host name noted above:

```conf
location /cas {
    proxy_pass https://cas.example.org:8443;
    proxy_set_header Host "sso.example.com";
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_cookie_path /cas/tenants/london /cas;
}
```

<div class="alert alert-info">:information_source: <strong>Note</strong><p>Make note of the
<code>proxy_cookie_path</code> setting which rewrite the cookie path for this tenant. This is
required because the cookie path is always scoped to the tenant URL by CAS, and subsequently
may not be made available to the browser since original request is passing through a reverse proxy.
</p></div>

You can build your own tenant routing and filtering mechanism via the following bean definition:

```java
@Bean
public FilterRegistrationBean tenantRoutingFilter() {
    var fr = new FilterRegistrationBean<MyTenantRoutingFilter>();
    /*
        fr.setFilter(new MyTenantRoutingFilter());
    */
    return fr;
}
```

{% endtab %}

{% tab multitenancyexamples Attribute Consent %}

The following tenant definition is allowed to define its 
own [attribute consent storage](../integration/Attribute-Release-Consent.html) via MongoDb. 

Depending on your choice of storage, the proper extension module must be 
included in your CAS deployment. As ever, please verify that the module does actually support multitenancy. 

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.consent.mongo.host": "localhost",
        "cas.consent.mongo.port": "27017",
        "cas.consent.mongo.user-id": "...",
        "cas.consent.mongo.password": "...",
        "cas.consent.mongo.collection": "TenantsConsentRepository",
        "cas.consent.mongo.authentication-database-name": "admin",
        "cas.consent.mongo.database-name": "consent"
      }
    }
  ]
]
```
    
In the event that the tenant does not specify its own attribute consent storage, the default storage 
defined globally in the CAS configuration for the entire CAS deployment will be used.

{% endtab %}

{% endtabs %}
          
