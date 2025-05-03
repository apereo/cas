---
layout: default
title: CAS - Multitenancy
category: Multitenancy
---
{% include variables.html %}

# Multitenancy

CAS supports the notion of multitenancy, where a single CAS server can be used to isolate parts of its configuration and policies
per each tenant that is assigned to a unique url to interact with the CAS server. Each tenant registered with CAS may have its own set of capabilities
such as authentication strategy and policies. Support for multitenancy is baked into CAS as a first class citizen
and you will need to configure CAS to enable the feature, register your tenants and define their capabilities.

{% include_cached casproperties.html properties="cas.multitenancy.core" %}

<div class="alert alert-info">:information_source: <strong>Status</strong><p>
Multitenancy is somewhat limited and new and is likely to evolve in future releases to support 
more use cases and capabilities for each tenant. Not every extension or feature in CAS may be 
immediately supported in a multitenant deployment.
</p></div>

When multitenancy is enabled, registered tenants will each receive their own dedicated url to access CAS:

```
/cas/tenants/{TENANT_ID}/...
```

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="multitenancy" %}

## Tenant Registration
                                                                      
Tenants are registered with CAS via a JSON file by default that is expected to be available at a well-known location
and automatically watched for changes.

{% include_cached casproperties.html properties="cas.multitenancy.json" %}

The basic construct for a tenant definition should match the following:

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "description": "This is my tenant description",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "key": "value"
      },
      "authenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantAuthenticationPolicy",
        "authenticationHandlers": [ "java.util.ArrayList", [ "LdapAuthHandler1" ] ],
        "authenticationProtocolPolicy": {
          "@class": "org.apereo.cas.multitenancy.TenantCasAuthenticationProtocolPolicy",
          "supportedProtocols": [ "java.util.HashSet", [ "SAML1", "CAS20", "CAS30" ] ]
        }
      },
      "delegatedAuthenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantDelegatedAuthenticationPolicy",
        "allowedProviders": [ "java.util.ArrayList", [ "..." ] ]
      },
      "communicationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantCommunicationPolicy",
        "emailCommunicationPolicy": {
          "@class": "org.apereo.cas.multitenancy.TenantEmailCommunicationPolicy",
          "from": "..."
        }
      },
      "userInterfacePolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantUserInterfacePolicy",
        "themeName": "shire"
      }
    }
  ]
]
```

### Custom Tenant Registration

If you need to customize the tenant registration process, you may do so by providing a custom implementation
of the following bean definition:

```java
@Bean
public TenantsManager tenantsManager() {
    return new MyTenantsManager();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## Tenant Capabilities

A registered tenant definition supports the following fields and capabilities:

| Field                           | Description                                                                                      |
|---------------------------------|--------------------------------------------------------------------------------------------------|
| `id`                            | Primary identifier for the tenant that forms the dedicated tenant URL.                           |
| `description`                   | Description of what this tenant is about.                                                        |
| `properties`                    | Map of CAS configuration properties effective for this tenant.                                   |
| `authenticationPolicy`          | Describes the criteria for primary authentication, list of allowed authentication handlers, etc. |
| `delegatedAuthenticationPolicy` | Describes the criteria for external authentication, list of allowed identity providers, etc.     |
| `communicationPolicy`           | Describes how the tenant should communicate with users to send out email messages, etc.          |
| `userInterfacePolicy`           | Describes how the tenant should control settings relevant for user interface pages.              |
  
### Authentication Policy
      
The tenant authentication policy supports the following fields:

| Field                    | Description                                                                                                   |
|--------------------------|---------------------------------------------------------------------------------------------------------------|
| `authenticationHandlers` | List of authentication handlers *pre-built* available to this tenant, invoked during authentication attempts. |
      
CAS features and modules that are multitenant-aware also have the ability to build their own list of authentication
handlers dynamically and on the fly without relying on the static list of authentication handlers that are bootstrapped
during startup, noted via the `authenticationHandlers` field above.

Custom authentication handlers that are built dynamically for each tenant may be defined using the following strategy:

```java
@Bean
public AuthenticationEventExecutionPlanConfigurer myTenantAuthentication() {
    return plan -> {
        var builder = new MyTenantAuthenticationHandlerBuilder(...);
        plan.registerTenantAuthenticationHandlerBuilder(builder);
    };
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

Please check the documentation for each feature or module to see if it supports multitenancy.

### Attribute Resolution

CAS features and modules that are multitenant-aware also have the ability to build their own list of attribute
repositories dynamically and on the fly without relying on repository implementations that are bootstrapped
during startup.

Custom attribute repositories that are built dynamically for each tenant may be defined using the following strategy:

```java
@Bean
public TenantPersonAttributeDaoBuilder myTenantPersonAttributeDaoBuilder() {
    return new MyTenantPersonAttributeDaoBuilder(..);
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

Please check the documentation for each feature or module to see if it supports multitenancy.

### Authentication Protocol Policy

The tenant authentication protocol policy controls specific aspects of a CAS-supported authentication protocol. Each policy setting
is captured inside a dedicated component that is responsible for managing the protocol settings and capabilities.
  
- CAS: `o.a.c.m.TenantCasAuthenticationProtocolPolicy`

| Field                | Description                                                                                                                               |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `supportedProtocols` | Set of [supported authentication protocols](../services/Configuring-Service-Supported-Protocols.html) that are owned by the CAS protocol. |
   
### Delegated Authentication Policy

The tenant delegated authentication policy controls aspects of CAS that support authentication 
[via external identity providers](../integration/Delegate-Authentication.html).

| Field              | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| `allowedProviders` | List of identity providers that are allowed and authorized for this tenant. |
          
### Communication Policy

The tenant communication policy controls per-tenant settings that describe email servers, SMS gateways, etc.
This construct allows one to isolate communication strategies per tenant.

- Email: `o.a.c.m.TenantEmailCommunicationPolicy`

| Field      | Description                                             |
|------------|---------------------------------------------------------|
| `from`     | The `FROM` address assigned to the email message sent.  |

### User Interface Policy

The tenant user interface policy controls per-tenant settings that describe a theme.
The theme defined will allow CAS to pull the appropriate theme 
resource [defined here](../ux/User-Interface-Customization-Themes-Static.html).
                
Furthermore, the theme definition is able to point to its own message bundle for various language keys:

```json
{
  "@class": "org.apereo.cas.multitenancy.TenantDefinition",
  "id": "shire",
  "description": "Example tenant",
  "properties": {
    "@class": "java.util.LinkedHashMap",
    "cas.message-bundle.base-names": "classpath:/shire_messages"
  }
}
```

Note that the tenant language bundle may only define what it actually requires. It is not necessary
to define the entire set of language keys that are available in the default CAS bundle. The default
bundles are still picked up to fill in the gaps for any missing keys.

### Tenant Properties

The tenant properties field is a map of CAS properties that are effective for this tenant. CAS features
and modules that do support multitenancy are able to read this map and apply the properties
to the tenant context. Examples here may include defining email server settings, authentication handler
construction and more. 

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
Not every CAS configuration property is multitenant-aware, and this capability is 
limited to CAS features and modules that are explicitly designed to support 
multitenancy. Support for multitenancy is evolving and new features modules may be added in future releases.
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

{% tab multitenancyexamples Virtual Hosts %}

CAS employs a special filter that is able to map an incoming request to a tenant definition based on the `Host`
header that is ultimately picked up by `HttpServletRequest#getServerName()`. A matching request will be routed
to the appropriate tenant url.
       
Assuming the `Host` header is given as `sso.example.org`, the `shire` tenant definition will allow
CAS to route requests from `https://sso.example.org/cas/login` to `https://${cas.server.domain}/cas/tenants/shire/login`.

And likewise, if the `Host` header is given as `sso.example.com`, the `mordor` tenant definition will allow
CAS to route requests from `https://sso.example.org/cas/login` to `https://${cas.server.domain}/cas/tenants/mordor/login`.

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
      "id": "morder",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "cas.host.name": "sso.example.com"
      }
    }
  ]
]
```
   
You can build your own tenant routing and filtering mechanism via:

```java
@Bean
public FilterRegistrationBean tenantRoutingFilter() {
    var fr = new FilterRegistrationBean<MyTenantRoutingFilter>();
    /*
        fr.setFilter(new MyTenantRoutingFilter());
    /*
    return fr;
}
```

{% endtab %}

{% endtabs %}
