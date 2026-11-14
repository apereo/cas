# Notifications - Google Firebase Cloud Messaging

Source: https://apereo.github.io/cas/development/notifications/Notifications-Configuration-FCM.html

Send push notifications to users’ devices through Google Firebase Cloud Messaging.

Support is enabled via the relevant modules using the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-notifications-fcm"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.google-firebase-messaging.registration-token-attribute-name` (Required; type `String`): The principal attribute name that contains the registration token for the user. Registration tokens that are provided by clients during the handshake process should be stored on the server, and made available to CAS as a principal attribute.
- `cas.google-firebase-messaging.service-account-key.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.google-firebase-messaging.database-url` (Optional; type `String`): Firebase database url.
- `cas.google-firebase-messaging.scopes` (Optional; type `List<String>`): Required scopes to properly communicate with the firebase cloud.
