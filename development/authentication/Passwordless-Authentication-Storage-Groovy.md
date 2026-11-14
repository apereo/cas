# Groovy Passwordless Authentication Storage

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Storage-Groovy.html

Look up passwordless user accounts, with their email address and phone number, through a Groovy script.

This strategy allows one to locate user records via a Groovy script. The body of the script may be defined as such:

```groovy
import org.apereo.cas.api.*

def run(Object[] args) {
    def (passwordlessRequest,logger) = args
    
    logger.info("Locating user record for user $passwordlessRequest")
    
    /*
     ...
     Locate the record for the given username, 
     and return the result back to CAS.
     ...
    */
    
    def account = new PasswordlessUserAccount()
    account.setUsername(passwordlessRequest.username)
    account.setEmail("username@example.org")
    account.setName("TestUser")
    account.setPhone("123-456-7890") 
    account.setAttributes(Map.of("...", List.of("...", "...")) 
    account.setMultifactorAuthenticationEligible(TriStateBoolean.FALSE)  
    account.setRequestPassword(false)
    return account
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.passwordless.accounts.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `passwordlessRequest` | The `PasswordlessAuthenticationRequest` object that represents the user. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)` |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
