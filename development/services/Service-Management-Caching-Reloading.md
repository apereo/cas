# Service Management - Caching

Source: https://apereo.github.io/cas/development/services/Service-Management-Caching-Reloading.html

CAS service definitions that are loaded from service registries are cached with a `expire-after-write` expiration policy. Such definition are automatically expired and removed from the cache, unless forcefully removed with an explicit reload operation. In particular, you want to make sure the cache expiration policy and period does not conflict with reload operations and schedules. For example, misconfiguration can lead to scenarios where the cache might be running empty while the scheduler is running a few minutes/seconds late. With an empty cache, authentication requests from applications might not be immediately authorized until the scheduled loader has had a chance to re-populate and reconstruct the cache. CAS logs a warning at startup when the cache expires service definitions before the scheduler reloads them; the cache duration should always be longer than the scheduler’s repeat interval.

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.cache.cache-size` (Optional; type `Long`; default `10000`): This cache size specifies the maximum number of entries the cache may contain. Note that the cache may evict an entry before this limit is exceeded or temporarily exceed the threshold while evicting. As the cache size grows close to the maximum, the cache evicts entries that are less likely to be used again. For example, the cache may evict an entry because it hasn't been used recently or very often. Note: to disable the cache, you may choose a cache size of `0`.
- `cas.service-registry.cache.duration` (Optional; type `String`; default `PT15M`): Cache duration specifies the fixed duration for an entry to be automatically removed from the cache after its creation.
- `cas.service-registry.cache.initial-capacity` (Optional; type `Integer`; default `1000`): This cache capacity sets the minimum total size for the internal data structures. Providing a large enough estimate at construction time avoids the need for expensive resizing operations later, but setting this value unnecessarily high wastes memory.

## Unmatched Services

When the [background scheduler](https://apereo.github.io/cas/development/services/Service-Management-Caching-Reloading.html#service-management---reloading) is enabled and the cache size is greater than zero, CAS matches the requested application against cached service definitions only. A request for an application that matches no cached definition is rejected without reading the service registry, so requests for unknown applications cannot force a read of the whole registry. A definition added to the registry outside this CAS server, such as by another CAS node or directly in the database or repository behind the registry, is recognized after the next scheduled reload.

- Definitions saved or deleted through CAS update the cache immediately, and so do files added, changed or removed in a [JSON](https://apereo.github.io/cas/development/services/JSON-Service-Management.html) or [YAML](https://apereo.github.io/cas/development/services/YAML-Service-Management.html) registry directory while its watcher is enabled.
- The service registry is still consulted for unmatched requests when the scheduler is disabled or the cache size is `0`.
- Lookups by numeric identifier or by name continue to consult the service registry when the cache has no match.

# Service Management - Reloading

CAS can be configured to load service definitions from connected sources and service registries on a schedule. Service definitions are loaded as background-running job, and the operation forces CAS to flush and invalidate cached version of service definitions and start anew.

> **Utility**
>
> The background scheduler reloads application definitions from a source regardless of the service registry type. It disregards the currently-loaded list of applications and fetches a new copy from the source, whatever that may be.

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.schedule.cron-expression` (Optional; type `String`): A cron-like expression, extending the usual UN*X definition to include triggers on the second, minute, hour, day of month, month, and day of week. For example, `0 * * * * MON-FRI` means once per minute on weekdays (at the top of the minute - the 0th second) or `0 0 0 * * *` means every day at midnight. * Note that when cron expressions are defined, the start delay and repeat interval settings are must be removed and set to blank.
- `cas.service-registry.schedule.cron-time-zone` (Optional; type `String`): A time zone for which the cron expression will be resolved. By default, this attribute is empty (i.e. the scheduler's time zone will be used).
- `cas.service-registry.schedule.enabled` (Optional; type `Boolean`; default `true`): Whether scheduler should be enabled to schedule the job to run.
- `cas.service-registry.schedule.enabled-on-host` (Optional; type `String`; default `.*`): Overrides `SchedulingProperties#enabled` property value of true if this property does not match hostname of CAS server. This can be useful if deploying CAS with an image in a statefulset where all names are predictable but where having different configurations for different servers is hard. The value can be an exact hostname or a regular expression that will be used to match the hostname.
- `cas.service-registry.schedule.repeat-interval` (Optional; type `String`; default `PT2M`): String representation of a repeat interval of re-loading data for a data store implementation. This is the timeout between consecutive job’s executions.
- `cas.service-registry.schedule.start-delay` (Optional; type `String`; default `PT15S`): String representation of a start delay of loading data for a data store implementation. This is the delay between scheduler startup and first job’s execution
