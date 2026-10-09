---
layout: default
title: CAS - Multitenancy
description: "Run several isolated tenants on one CAS server, each with its own URL, authentication strategy, policies and settings."
category: Multitenancy
---
{% include variables.html %}

# Multitenancy

CAS supports the notion of multitenancy, where a single CAS server can be used to isolate parts of its configuration
and policies per each tenant that is assigned to a unique url to interact with the CAS server. Each tenant registered 
with CAS may have its own set of capabilities such as authentication strategy and policies. Support for multitenancy 
is baked into CAS as a first class citizen and you will need to configure CAS to enable the feature, register your 
tenants and define their capabilities.

{% include_cached casproperties.html properties="cas.multitenancy.core" %}

<div class="alert alert-info">:information_source: <strong>Status</strong><p>
Multitenancy is somewhat limited, new and is likely to evolve and change in future releases to support 
more use cases and capabilities for each tenant. Not every extension or feature in CAS may be 
immediately supported in a multitenant deployment.
</p></div>

When multitenancy is enabled, registered tenants will each receive their own dedicated url to access CAS:

```
/cas/tenants/{TENANT_ID}/...
```
 
Furthermore, in some cases you may also be able to pass along the tenant id as a request header, 
and CAS will be able to resolve the tenant definition. This extraction logic is particularly employed
for actuator endpoints that do support multitenancy:

```bash
curl --location 
    'https://sso.example.org/cas/actuator/{ACTUATOR}' \
    --header 'X-Tenant-Id: {TENANT_ID}'
```

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="multitenancy" %}

## Topics

- [Tenant Registration](Multitenancy-Tenant-Registration.html): where tenant definitions come from.
- [Tenant Capabilities](Multitenancy-Tenant-Capabilities.html): the policies a tenant can define.
- [Tenant Properties](Multitenancy-Tenant-Properties.html): CAS settings that apply to a single tenant.
- [Performance](Multitenancy-Performance.html): the cost of resolving tenant resources at runtime.
