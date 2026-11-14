# Multifactor Authentication Trusted Device/Browser - Cleaning

Source: https://apereo.github.io/cas/development/mfa/Multifactor-TrustedDevice-Authentication-Cleaner.html

User decisions must be remembered and processed later on subsequent requests. A background *cleaner* process is also automatically scheduled to scan the chosen repository/database/registry periodically and remove expired records based on configured threshold parameters.

> **Cleaner Usage**
>
> In a clustered CAS deployment, it is best to keep the cleaner running on one designated CAS node only and turn it off on all others via CAS settings. Keeping the cleaner running on all nodes may likely lead to severe performance and locking issues.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.cleaner.schedule.cron-expression` (Optional; type `String`): A cron-like expression, extending the usual UN*X definition to include triggers on the second, minute, hour, day of month, month, and day of week. For example, `0 * * * * MON-FRI` means once per minute on weekdays (at the top of the minute - the 0th second) or `0 0 0 * * *` means every day at midnight. * Note that when cron expressions are defined, the start delay and repeat interval settings are must be removed and set to blank.
- `cas.authn.mfa.trusted.cleaner.schedule.cron-time-zone` (Optional; type `String`): A time zone for which the cron expression will be resolved. By default, this attribute is empty (i.e. the scheduler's time zone will be used).
- `cas.authn.mfa.trusted.cleaner.schedule.enabled` (Optional; type `Boolean`; default `true`): Whether scheduler should be enabled to schedule the job to run.
- `cas.authn.mfa.trusted.cleaner.schedule.enabled-on-host` (Optional; type `String`; default `.*`): Overrides `SchedulingProperties#enabled` property value of true if this property does not match hostname of CAS server. This can be useful if deploying CAS with an image in a statefulset where all names are predictable but where having different configurations for different servers is hard. The value can be an exact hostname or a regular expression that will be used to match the hostname.
- `cas.authn.mfa.trusted.cleaner.schedule.repeat-interval` (Optional; type `String`; default `PT2M`): String representation of a repeat interval of re-loading data for a data store implementation. This is the timeout between consecutive job’s executions.
- `cas.authn.mfa.trusted.cleaner.schedule.start-delay` (Optional; type `String`; default `PT15S`): String representation of a start delay of loading data for a data store implementation. This is the delay between scheduler startup and first job’s execution
