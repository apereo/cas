# JSON Passwordless Authentication Storage

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Storage-JSON.html

This strategy allows one to locate user records via a JSON resource, as such:

```json
{
  "@class" : "java.util.LinkedHashMap",
  "casuser@\\w+.com" : {
    "@class": "org.apereo.cas.api.PasswordlessUserAccount",
    "username": "casuser",
    "attributes": {
        "@class": "java.util.LinkedHashMap",
        "name": [ "java.util.ArrayList", ["value"] ]
    },
    "multifactorAuthenticationEligible": "TRUE",
    "delegatedAuthenticationEligible": "TRUE",
    "allowedDelegatedClients" : [ "java.util.ArrayList", [ "ClientName1" ] ],
    "requestPassword": false,
    "allowSelectionMenu": false
  }
}
```

> **Note**
>
> Note that the key specified in the above block can be a regular expression pattern.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.passwordless.accounts.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
