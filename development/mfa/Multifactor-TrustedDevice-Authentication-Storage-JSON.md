# JSON Device Storage - Multifactor Authentication Trusted Device/Browser

Source: https://apereo.github.io/cas/development/mfa/Multifactor-TrustedDevice-Authentication-Storage-JSON.html

Records may be kept inside a static json resource whose path is defined via CAS settings. This is also most useful if you have a very small deployment with a small user base or if you wish to demo the functionality.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
