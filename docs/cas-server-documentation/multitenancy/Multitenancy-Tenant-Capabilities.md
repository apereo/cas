---
layout: default
title: CAS - Multitenancy - Tenant Capabilities
description: "The fields of a tenant definition: authentication, attribute resolution, protocol, delegated authentication and user interface policies."
category: Multitenancy
---

{% include variables.html %}

# Tenant Capabilities - Multitenancy

A tenant definition controls how CAS behaves for that tenant. This page lists the policies a tenant can define; settings that apply to individual CAS features are covered in [Tenant Properties](Multitenancy-Tenant-Properties.html).

A registered tenant definition supports the following fields and capabilities:

| Field                           | Description                                                                                                            |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `id`                            | Primary identifier for the tenant that forms the dedicated tenant URL.                                                 |
| `description`                   | Description of what this tenant is about.                                                                              |
| `properties`                    | Map of CAS configuration properties effective for this tenant. Remember that not all properties are multitenant aware. |
| `authenticationPolicy`          | Describes the criteria for primary authentication, list of allowed authentication handlers, etc.                       |
| `delegatedAuthenticationPolicy` | Describes the criteria for external authentication, list of allowed identity providers, etc.                           |
| `userInterfacePolicy`           | Describes how the tenant should control settings relevant for user interface pages.                                    |
  
## Authentication Policy
      
The tenant authentication policy supports the following fields:

| Field                    | Description                                                                                                   |
|--------------------------|---------------------------------------------------------------------------------------------------------------|
| `authenticationHandlers` | List of authentication handlers *pre-built* available to this tenant, invoked during authentication attempts. |
| `attributeRepositories`  | List of attribute repositories *pre-built* available to this tenant, invoked during authentication attempts.  |
      
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

## Attribute Resolution

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

## Authentication Protocol Policy

The tenant authentication protocol policy controls specific aspects of a CAS-supported authentication protocol. Each policy setting
is captured inside a dedicated component that is responsible for managing the protocol settings and capabilities.
  
- CAS: `o.a.c.m.TenantCasAuthenticationProtocolPolicy`

| Field                | Description                                                                                                                               |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `supportedProtocols` | Set of [supported authentication protocols](../services/Configuring-Service-Supported-Protocols.html) that are owned by the CAS protocol. |
   
## Delegated Authentication Policy

The tenant delegated authentication policy controls aspects of CAS that support authentication 
[via external identity providers](../integration/Delegate-Authentication.html).

| Field              | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| `allowedProviders` | List of identity providers that are allowed and authorized for this tenant. |

## User Interface Policy

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
