# Groovy Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-Groovy.html

The following configuration describes how to fetch and retrieve attributes from Groovy attribute repositories.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.groovy[0].location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.attribute-repository.groovy` (Optional; type `List<GroovyPrincipalAttributesProperties>`): Retrieve attributes from multiple Groovy scripts.
- `cas.authn.attribute-repository.groovy[0].id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.groovy[0].order` (Optional; type `int`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.groovy[0].state` (Optional; type `AttributeRepositoryStates`; default `ACTIVE`): Whether attribute resolution based on this source is enabled. Available values are as follows: - `ACTIVE`: Active and enabled repository, and is invoked by default automatically. - `DISABLED`: Attribute repository is disabled and will not be used to resolve people and attributes. - `STANDBY`: Repository is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for and will not be registered into the resolution plan.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The Groovy script may be designed as:

```groovy
import java.util.*

def run(final Object... args) {
    def (username,attributes,logger,properties,appContext) = args
    logger.debug("[{}]: The received uid is [{}]", this.class.simpleName, username)
    
    // All attribute values must be defined as a collection wrapped in []
    return [username:[username], likes:["cheese", "food"], id:[1234,2,3,4,5], another:["attribute"] ]
}
```

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `username` | Current principal identifier found from the authentication phase. |
| `attributes` | `Map` of query attributes built by CAS to construct the attribute resolution query. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
| `properties` | CAS configuration properties. |
| `appContext` | The current and active application context under the type `ApplicationContext`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
