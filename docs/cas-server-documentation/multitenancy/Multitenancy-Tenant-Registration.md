---
layout: default
title: CAS - Multitenancy - Tenant Registration
description: "How tenants are registered with CAS: the JSON tenant definition file, and how to supply your own tenant registry."
category: Multitenancy
---

{% include variables.html %}

# Tenant Registration - Multitenancy

Each tenant must be registered with CAS before it can be used. Tenant definitions are read from a JSON file by default, and you can replace that with your own tenant registry.

                                                                      
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
        "attributeRepositories": [ "java.util.ArrayList", [ "AttributeRepository1" ] ],
        "authenticationProtocolPolicy": {
          "@class": "org.apereo.cas.multitenancy.TenantCasAuthenticationProtocolPolicy",
          "supportedProtocols": [ "java.util.HashSet", [ "SAML1", "CAS20", "CAS30" ] ]
        }
      },
      "delegatedAuthenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantDelegatedAuthenticationPolicy",
        "allowedProviders": [ "java.util.ArrayList", [ "..." ] ]
      },
      "userInterfacePolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantUserInterfacePolicy",
        "themeName": "shire"
      }
    }
  ]
]
```

Tenant ids become part of urls and cookie paths. An id must start with a letter or a digit and may contain only
letters, digits, dots, dashes and underscores; tenant definitions with other ids are skipped when tenant definitions are
loaded, and refused when they are registered.

## Custom Tenant Registration

If you need to customize the tenant registration process, you may do so by providing a custom implementation
of the following bean definition:

```java
@Bean
public TenantsManager tenantsManager() {
    return new MyTenantsManager();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
