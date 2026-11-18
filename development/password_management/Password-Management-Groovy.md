# Password Management - Groovy

Source: https://apereo.github.io/cas/development/password_management/Password-Management-Groovy.html

Look up accounts, change passwords and handle security questions with a Groovy script.

Accounts and password may be determined and handled using a customized Groovy script. The outline of the script may match the following:

```groovy
import org.apereo.cas.pm.*

def change(Object[] args) {
    def (passwordChangeBean,logger) = args
    return true
}

def findEmails(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return "cas@example.org"
}

def findPhone(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return "1234567890"
}

def findUsername(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return "casuser"
}

def getSecurityQuestions(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return [securityQuestion1: "securityAnswer1"]
}

def updateSecurityQuestions(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    // Execute update...
}

def unlockAccount(Object[] args) {
    def (credential,logger) = args
    // Execute unlock...
    return true
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pm.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
