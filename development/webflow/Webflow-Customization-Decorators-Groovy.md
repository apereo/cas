# Groovy Decorators - Webflow Decorations

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Decorators-Groovy.html

Add data to the CAS login page with a Groovy script, for example announcements fetched from another system.

Groovy login decorators allow one to inject data into the Spring webflow context by using an external Groovy script that may take on the following form:

```groovy
def run(Object[] args) {
    def (requestContext,logger) = args
    logger.info("Decorating the webflow...")
    requestContext.flowScope.put("decoration", ...)
 }
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `requestContext` | The `RequestContext` that carries various types of scopes as data containers. |
| `logger` | Logger object used to issue log messages where needed. |

The following settings and properties are available from the CAS configuration catalog:

- `cas.webflow.login-decorator.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
