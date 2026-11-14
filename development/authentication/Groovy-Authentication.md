# Groovy Authentication

Source: https://apereo.github.io/cas/development/authentication/Groovy-Authentication.html

Verify and authenticate credentials using Groovy scripts. The task of credential verification, principal transformation, handling password policy and all other related matters are the sole responsibility of the Groovy script.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-generic"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.groovy.name` (Optional; type `String`): Authentication handler name used to verify credentials in the file.
- `cas.authn.groovy.order` (Optional; type `Integer`): Order of the authentication handler in the chain.
- `cas.authn.groovy.state` (Optional; type `AuthenticationHandlerStates`; default `active`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The Groovy script may be designed as:

```groovy
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.credential.*
import org.apereo.cas.authentication.metadata.*

import javax.security.auth.login.*

def authenticate(final Object... args) {
    def (authenticationHandler,credential,servicesManager,principalFactory,logger) = args

    /*
     * Figure out how to verify credentials...
     */
    if (authenticationWorksCorrectly()) {
        def principal = principalFactory.createPrincipal(credential.username);
        return new DefaultAuthenticationHandlerExecutionResult(authenticationHandler,
            credential, principal, new ArrayList<>());
    }
    throw new FailedLoginException();
}

def supportsCredential(final Object... args) {
    def (credential,logger) = args
    return credential != null
}

def supportsCredentialClass(final Object... args) {
    def (credentialClazz,logger) = args
    return credentialClazz == UsernamePasswordCredential.class
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
