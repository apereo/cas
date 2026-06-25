---
layout: default
title: CAS - Service Management
category: Services
---

{% include variables.html %}

# Service Management - Caching

CAS service definitions that are loaded from service registries are cached with a `expire-after-write` expiration policy.
Such definition are automatically expired and removed from the cache, unless forcefully removed with an explicit reload operation.
In particular, you want to make sure the cache expiration policy and period does not conflict with reload operations and schedules. 
For example, misconfiguration can lead to scenarios where the cache might be running empty while the scheduler is running a
few minutes/seconds late. With an empty cache, authentication requests from applications might not be immediately authorized
util the scheduled loader has had a chance to re-populate and reconstruct the cache.

{% include_cached casproperties.html properties="cas.service-registry.cache" %}

# Service Management - Reloading

CAS can be configured to load service definitions from connected sources and service registries on a schedule. Service definitions
are loaded as background-running job, and the operation forces CAS to flush and invalidate cached version of service definitions
and start anew.

<div class="alert alert-info">:information_source: <strong>Utility</strong><p>
The background scheduler reloads application definitions from a source regardless of the service registry type.
It disregards the currently-loaded list of applications and fetches a new copy from the source,
whatever that may be.
</p></div>

{% include_cached casproperties.html properties="cas.service-registry.schedule" %}
