---
layout: default
title: CAS - Service Management - Caching
description: "How CAS caches service definitions loaded from registries, and how to reload them on a schedule or on demand."
category: Services
---

{% include variables.html %}

# Service Management - Caching

CAS service definitions that are loaded from service registries are cached with a `expire-after-write` expiration policy.
Such definition are automatically expired and removed from the cache, unless forcefully removed with an explicit reload operation.
In particular, you want to make sure the cache expiration policy and period does not conflict with reload operations and schedules. 
For example, misconfiguration can lead to scenarios where the cache might be running empty while the scheduler is running a
few minutes/seconds late. With an empty cache, authentication requests from applications might not be immediately authorized
until the scheduled loader has had a chance to re-populate and reconstruct the cache. CAS logs a warning at startup
when the cache expires service definitions before the scheduler reloads them; the cache duration should always be
longer than the scheduler's repeat interval.

{% include_cached casproperties.html properties="cas.service-registry.cache" %}

## Unmatched Services

When the [background scheduler](#service-management---reloading) is enabled and the cache size is greater than zero,
CAS matches the requested application against cached service definitions only. A request for an application that
matches no cached definition is rejected without reading the service registry, so requests for unknown applications
cannot force a read of the whole registry. A definition added to the registry outside this CAS server, such as by another
CAS node or directly in the database or repository behind the registry, is recognized after the next scheduled reload.

- Definitions saved or deleted through CAS update the cache immediately, and so do files added, changed or removed in a
  [JSON](JSON-Service-Management.html) or [YAML](YAML-Service-Management.html) registry directory while its watcher is enabled.
- The service registry is still consulted for unmatched requests when the scheduler is disabled or the cache size is `0`.
- Lookups by numeric identifier or by name continue to consult the service registry when the cache has no match.

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
