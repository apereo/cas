---
layout: default
title: CAS - Multitenancy - Performance
description: "How CAS builds tenant resources at runtime, what that costs under load, and how many active tenants the current design suits."
category: Multitenancy
---

{% include variables.html %}

# Performance - Multitenancy

CAS resolves the tenant on every request and builds the connections that tenant needs on the fly. This page explains the cost of that design and when it starts to matter.

The current multitenant design resolves the tenant at runtime and dynamically builds the resource access 
path needed for that tenant. The target resource may be a JDBC database, REST API, LDAP directory, message broker, 
remote service, or another tenant-specific integration point. This keeps the system flexible and avoids eagerly 
registering a large number of tenant-specific Spring components, especially when tenants may each choose different 
endpoints, credentials, protocols, storage backends, or operational policies. It also avoids assuming that 
all tenants are active at the same time.

The main caveat with this design is performance under concurrent activity. If each tenant 
operation creates a new resource client, establishes a connection, authenticates, executes the operation,
and then immediately tears everything down, the system repeatedly pays the cost of setup and cleanup. 
Depending on the resource type, this overhead may include TCP connection establishment, TLS negotiation, 
authentication or bind operations, connection validation, session initialization, client construction, 
metadata discovery, and remote service cleanup. For a small number of tenants with light or sporadic 
usage, this may be acceptable. However, with moderately active tenants, especially when CAS flows perform 
multiple tenant-backed operations per request, repeated setup and teardown can introduce latency spikes 
and increase load on both CAS and the downstream tenant resources.

For a deployment with a relatively small number of tenants, such as around 20, the current approach can 
be viable if traffic is moderate, resource setup is fast, downstream systems are nearby and reliable, 
and the number of tenant-specific operations per CAS request is low. That said, the risk increases with 
concurrent logins, service lookups, consent checks, profile lookups, attribute resolution, audit writes, 
or other operations that occur during authentication and protocol flows. The determining factor is less the 
total number of configured tenants and more the number of active tenants, concurrent requests per tenant, 
tenant-specific operations per request, and the average and tail latency of each full setup/operation/teardown cycle.

A possible future improvement is to retain the dynamic tenant-resolution model 
but avoid tearing down expensive resource infrastructure immediately after every operation. 
Instead, CAS could use a lazy, bounded, tenant-aware registry or cache for resource clients and 
connection-capable components. Tenant-specific resources would be created only when first needed, 
reused while the tenant remains active, and closed after an idle timeout, health failure, configura
tion change, or cache eviction. For resources that support pooling, such as JDBC, LDAP, HTTP, 
or messaging clients, small pools with conservative limits can allow active tenants to benefit 
from reuse while preventing inactive tenants from consuming resources indefinitely.

This future possible approach provides a better balance between flexibility and performance. It avoids the cost and complexity 
of eagerly registering every possible tenant resource as a Spring bean, while also avoiding the latency and 
churn of creating and destroying expensive clients or connections on every request. Over time, the implementation 
can be enhanced with global resource limits, idle eviction, per-tenant metrics, health checks, circuit breakers 
for unhealthy downstream systems, connection and request timeout controls, configuration refresh support, 
secret rotation support, and operational safeguards to prevent one tenant from exhausting shared CAS 
resources or overwhelming downstream services.

We might consider improving this design in future iterations based on community demand and deployment experience.
