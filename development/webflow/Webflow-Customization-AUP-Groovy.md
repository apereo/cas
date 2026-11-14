# Groovy Acceptable Usage Policy

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-Groovy.html

Alternatively, CAS can be configured to use a Groovy script to verify status of policies and store results.

The following settings and properties are available from the CAS configuration catalog:

- `cas.acceptable-usage-policy.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

The script should match the following:

```groovy
import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.apereo.cas.aup.*
import org.springframework.webflow.execution.*

def verify(Object[] args) {
    def (requestContext,applicationContext,principal,logger) = args
    ...
    if (policyAccepted()) {
        return AcceptableUsagePolicyStatus.accepted(principal)
    }
    return AcceptableUsagePolicyStatus.denied(principal)
}

def submit(Object[] args) {
    def (requestContext,applicationContext,principal,logger) = args
    ...
    return true
}
     
/*
    A special callback function is implemented
    as an override to return an `AcceptableUsagePolicyTerms` 
    object back to CAS to be re-purposed
    for acceptable usage policy flows.
*/
def fetch(Object[] args) {
    def (requestContext,applicationContext,principal,logger) = args
    ...
    return AcceptableUsagePolicyTerms.builder()
            .defaultText("Hello, World")
            .code(AcceptableUsagePolicyTerms.CODE)
            .build()
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `requestContext` | The object representing the Spring Webflow `RequestContext`. |
| `applicationContext` | The object representing the Spring `ApplicationContext`. |
| `principal` | The object representing the authenticated `Principal`. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
