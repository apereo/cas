# Account (Self-Service) Registration - Groovy Provisioning

Source: https://apereo.github.io/cas/development/registration/Account-Registration-Provisioning-Groovy.html

Account registration requests can be submitted to an external Groovy script that is responsible for managing and storing the account in the appropriate systems of record.

The following settings and properties are available from the CAS configuration catalog:

- `cas.account-registration.provisioning.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The script should match the following:

```groovy
import org.apereo.cas.acct.*
import org.springframework.context.ApplicationContext

def run(Object[] args) {
    def (registrationRequest,applicationContext,logger) = args
    
    logger.info("Provisioning account registration request ${registrationRequest}")
    return AccountRegistrationResponse.success()
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `registrationRequest` | The object representing the account registration request. |
| `applicationContext` | The object representing the Spring `ApplicationContext`. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
