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
      "multifactorAuthenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantMultifactorAuthenticationPolicy",
        "globalProviderIds": [ "java.util.ArrayList", [ "..." ] ]
      },
      "communicationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantCommunicationPolicy",
        "emailCommunicationPolicy": {
          "@class": "org.apereo.cas.multitenancy.TenantEmailCommunicationPolicy",
          "host": "...",
          "port": 2500,
          "from": "..."
        }
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

| Field                             | Description                                                                                      |
|-----------------------------------|--------------------------------------------------------------------------------------------------|
| `id`                              | Primary identifier for the tenant that forms the dedicated tenant URL.                           |
| `description`                     | Description of what this tenant is about.                                                        |
| `authenticationPolicy`            | Describes the criteria for primary authentication, list of allowed authentication handlers, etc. |
| `delegatedAuthenticationPolicy`   | Describes the criteria for external authentication, list of allowed identity providers, etc.     |
| `multifactorAuthenticationPolicy` | Describes the criteria for multifactor authentication, list of multifactor providers, etc.       |
| `communicationPolicy`             | Describes how the tenant should communicate with users to send out email messages, etc.          |
  
### Authentication Policy
      
The tenant authentication policy supports the following fields:

| Field                    | Description                                                                                       |
|--------------------------|---------------------------------------------------------------------------------------------------|
| `authenticationHandlers` | List of authentication handlers available to this tenant, invoked during authentication attempts. |

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
          
### Multifactor Authentication Policy

The tenant multifactor authentication policy controls aspects of CAS that activate and enforce 
[multifactor authentication](../mfa/Configuring-Multifactor-Authentication.html).

| Field               | Description                                                                |
|---------------------|----------------------------------------------------------------------------|
| `globalProviderIds` | List of multifactor provider IDs that should be activated for this tenant. |
                  
### Communication Policy

The tenant communication policy controls per-tenant settings that describe email servers, SMS gateways, etc.
This construct allows one to isolate communication strategies per tenant.

- Email: `o.a.c.m.TenantEmailCommunicationPolicy`

| Field      | Description                                             |
|------------|---------------------------------------------------------|
| `host`     | Email server host name, i.e. `localhost`.               |
| `port`     | Email server port activated when set to non-zero value. |
| `username` | Email server username to establish connections.         |
| `password` | Email server password to establish connections.         |
| `from`     | The `FROM` address assigned to the email message sent.  |
  
