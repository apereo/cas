# Password Management - JSON

Source: https://apereo.github.io/cas/development/password_management/Password-Management-JSON.html

Accounts and password may be stored inside a static modest JSON resource, whose location is taught to CAS via settings. This option is most useful during development, testing and demos and is not suitable for production.

The outline of the JSON file may match the following:

```json
{
  "casuser" : {
    "email" : "casuser@example.org",
    "password" : "p@ssw0rd",
    "phone" : "1234567890",
    "status": "OK",
    "securityQuestions" : {
      "question1" : "answer1",
      "question2" : "answer2"
    }
  }
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pm.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
