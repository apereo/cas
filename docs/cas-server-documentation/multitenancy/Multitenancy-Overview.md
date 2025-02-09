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
      "description": "This is the tenant definition",
      "authenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantAuthenticationPolicy",
        "authenticationHandlers": [
          "java.util.ArrayList",
          [
            "..."
          ]
        ]
      },
      "delegatedAuthenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantDelegatedAuthenticationPolicy",
        "allowedProviders": [
          "java.util.ArrayList",
          [
            "..."
          ]
        ]
      }
    }
  ]
]
```

A registered tenant definition supports the following fields:

| Field                           | Description                                                                                      |
|---------------------------------|--------------------------------------------------------------------------------------------------|
| `id`                            | Primary identifier for the tenant that forms the dedicated tenant URL.                           |
| `description`                   | Description of what this tenant is about.                                                        |
| `authenticationPolicy`          | Describes the criteria for primary authentication, list of allowed authentication handlers, etc. |
| `delegatedAuthenticationPolicy` | Describes the criteria for external authentication, list of allowed identity providers, etc.     |
     

### Custom Tenant Registration

If you need to customize the tenant registration process, you may do so by providing a custom implementation
of the following bean definition:

```java
@Bean
public TenantsManager tenantsManager() {
    return new MyTenantsManager();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
