# Password History - Password Management

Source: https://apereo.github.io/cas/development/password_management/Password-Management-History.html

CAS allows for strategies to track and storage recycled password. Recycled passwords are kept in storage for the user account and are examined upon password updates for validity.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pm.history.core.enabled` (Optional; type `Boolean`; default `false`): Flag to indicate if password history tracking is enabled.

Once password history functionality is enabled, passwords can be tracked in history via a Groovy or an in-memory backend. Specific storage options may also provide their own support for password history.

**Groovy**

Password history tracking, once enabled, can be handed off to an external Groovy script as such:

```groovy
def exists(Object[] args) {
    def (request,logger) = args
    return false
}

def store(Object[] args) {
    def (request,logger) = args
    return true
}

def fetchAll(Object[] args) {
    def (logger) = args
    return []
}

def fetch(Object[] args) {
    def (username,logger) = args
    return []
}   

def remove(Object[] args) { 
    def (username,logger) = args
}

def removeAll(Object[] args) { 
    def (logger) = args
}
```

The `request` parameter encapsulates a `PasswordChangeRequest` object, carrying `username` and `password` fields.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pm.history.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

**Custom**

If you wish to create your own password history service, you will need to design a component and register it with CAS as such:

```java
@Bean
public PasswordHistoryService passwordHistoryService() {
    return new CustomPasswordHistoryService();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
