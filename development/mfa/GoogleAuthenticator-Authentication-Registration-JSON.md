# JSON Google Authenticator Registration

Source: https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication-Registration-JSON.html

Registration records may also be kept inside a single JSON file for all users. The behavior is only activated when a path to a JSON data store file is provided, and otherwise CAS may fallback to keeping records in memory. This feature is mostly useful during development and for demo purposes.

> **Single Node**
>
> Changes to the JSON file are coordinated within a single CAS server only. Do not point several CAS nodes at the same file, since concurrent updates from different nodes may overwrite one another.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.gauth.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
