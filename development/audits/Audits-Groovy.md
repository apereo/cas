# Groovy Audits

Source: https://apereo.github.io/cas/development/audits/Audits-Groovy.html

Groovy-based audits have the ability to receive and process the auditable context parameters and build the final auditable record in any text format or representation. The final auditable record is then passed to the logging framework, typically tagged under `INFO`.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.audit.groovy.template.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Scripts

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `applicationContext` | The object representing the Spring application context. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
| `clientIpAddress` | self-explanatory. |
| `serverIpAddress` | self-explanatory. |
| `what` | self-explanatory. |
| `who` | self-explanatory. |
| `when` | self-explanatory. |
| `action` | self-explanatory. |
| `userAgent` | self-explanatory. |
| `application` | self-explanatory. |
| `geoLocation` | self-explanatory. |
| HTTP Request headers | All collected headers are passed by their name. |
| *Extra Info* | Arbitrary keys/names collected by the audit engine from various components. |

A sample script follows:

```groovy
${logger.info("Hello, World")}

who: ${who}, what: ${what}, when: ${when}, ip: ${clientIpAddress}, trace: ${customHttpRequestHeader}
```
