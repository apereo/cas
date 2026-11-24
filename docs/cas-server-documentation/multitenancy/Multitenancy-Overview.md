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

A request that carries a tenant header and a tenant url naming different tenants is refused with a bad request.

## Tenant Isolation

Tickets and single sign-on sessions belong to the tenant in which they were created. A single sign-on session
established at one tenant's url, or outside of any tenant, does not carry over to another tenant: the user is asked
to log in again, and the existing session cannot obtain service or proxy tickets for the applications of another tenant.
Applications that belong to a tenant validate service tickets and request proxy tickets at the url of that tenant,
such as `/cas/tenants/{TENANT_ID}/p3/serviceValidate` and `/cas/tenants/{TENANT_ID}/proxy`, or by passing the tenant
header; a service ticket issued for one tenant, or outside of any tenant, is refused elsewhere.

Multifactor authentication devices registered with Google Authenticator and attribute consent decisions also belong to
the tenant in which they were registered or made, even when all tenants share the same storage. A device registered at
one tenant's url does not answer multifactor authentication, and is not listed or managed, at another tenant's url, and a
consent decision made for an application in one tenant does not apply to that application in another tenant. Devices
and decisions recorded outside of any tenant apply only outside of any tenant. A login that is in progress at one tenant's
url cannot be continued at another tenant's url either.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="multitenancy" %}

The endpoint does not disclose secrets. Property values whose names look like a password, secret, credential, key, token
or passphrase, and the static list of users and passwords, are shown as `******`. When a tenant definition is posted back with such a masked value, the value already
stored for that property is kept, so a definition can be read, changed and posted back without retyping its secrets.
Encrypted values and expressions in the tenant definitions are also written back as they were stored, and are not
replaced with the values they resolve to.

## Topics

- [Tenant Registration](Multitenancy-Tenant-Registration.html): where tenant definitions come from.
- [Tenant Capabilities](Multitenancy-Tenant-Capabilities.html): the policies a tenant can define.
- [Tenant Properties](Multitenancy-Tenant-Properties.html): CAS settings that apply to a single tenant.
- [Performance](Multitenancy-Performance.html): the cost of resolving tenant resources at runtime.
