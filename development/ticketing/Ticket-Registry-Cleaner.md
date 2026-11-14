# Ticket Registry Cleaner

Source: https://apereo.github.io/cas/development/ticketing/Ticket-Registry-Cleaner.html

A background *cleaner* process is automatically scheduled to scan the chosen registry implementation periodically and remove expired records based on configured threshold parameters.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.cleaner.schedule.cron-expression` (Optional; type `String`): A cron-like expression, extending the usual UN*X definition to include triggers on the second, minute, hour, day of month, month, and day of week. For example, `0 * * * * MON-FRI` means once per minute on weekdays (at the top of the minute - the 0th second) or `0 0 0 * * *` means every day at midnight. * Note that when cron expressions are defined, the start delay and repeat interval settings are must be removed and set to blank.
- `cas.ticket.registry.cleaner.schedule.cron-time-zone` (Optional; type `String`): A time zone for which the cron expression will be resolved. By default, this attribute is empty (i.e. the scheduler's time zone will be used).
- `cas.ticket.registry.cleaner.schedule.enabled` (Optional; type `Boolean`; default `true`): Whether scheduler should be enabled to schedule the job to run.
- `cas.ticket.registry.cleaner.schedule.enabled-on-host` (Optional; type `String`; default `.*`): Overrides `SchedulingProperties#enabled` property value of true if this property does not match hostname of CAS server. This can be useful if deploying CAS with an image in a statefulset where all names are predictable but where having different configurations for different servers is hard. The value can be an exact hostname or a regular expression that will be used to match the hostname.
- `cas.ticket.registry.cleaner.schedule.repeat-interval` (Optional; type `String`; default `PT2M`): String representation of a repeat interval of re-loading data for a data store implementation. This is the timeout between consecutive job’s executions.
- `cas.ticket.registry.cleaner.schedule.start-delay` (Optional; type `String`; default `PT15S`): String representation of a start delay of loading data for a data store implementation. This is the delay between scheduler startup and first job’s execution

The ticket registry cleaner is generally useful in scenarios where the registry implementation is unable to auto-evict expired tokens and entries on its own via a background task. It may also be useful in scenarios where the configured ticket expiration policy in CAS cannot be a direct one-to-one match for the ticket registry if the policy is too parameterized or has many other dynamic conditions that would not be directly translatable for the ticket registry API.

Each run reads expired tickets in batches. Registries that store when each ticket expires, such as the [JPA ticket registry](https://apereo.github.io/cas/development/ticketing/JPA-Ticket-Registry.html), are asked only for tickets whose expiration time has passed. The first run after startup and every sixtieth run after it (hourly with the default one-minute schedule) read every ticket instead, to catch tickets that expire for reasons other than time. Other registries return every ticket on each run, as before; every ticket read is still checked against its expiration policy before it is removed.

Note that CAS itself will remove expired tickets on-demand when a ticket object is fetched and being processed. The ticket registry cleaner use case primarily addresses stale tickets that would otherwise never be requested and processed to go through the on-demand cleaning process as necessary.

> **Cleaner Usage**
>
> In a clustered CAS deployment, it is best to keep the cleaner running on one designated CAS node only and turn it off on all others via CAS settings. Keeping the cleaner running on all nodes may likely lead to severe performance and locking issues.
