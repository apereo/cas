# OpenID Connect Authentication JWKS Storage - Groovy

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage-Groovy.html

Let a Groovy script generate and return the JSON Web Key Set that CAS uses to sign and encrypt OpenID Connect tokens.

Keystore generation can be outsourced to an external Groovy script whose body should be defined as such:

```groovy
import org.apereo.cas.oidc.jwks.*
import org.jose4j.jwk.*

def run(Object[] args) {
    def (logger) = args
    logger.info("Generating JWKS for CAS...")
    def jsonWebKeySet = "{ \"keys\": [...] }"
    return jsonWebKeySet
}

def store(Object[] args) {
    def (jwks,logger) = args
    logger.info("Storing JWKS for CAS...")
    return jwks
}

def find(Object[] args) {
    def (logger) = args
    logger.info("Looking up JWKS...")
    return new JsonWebKeySet(...)
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.jwks.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
