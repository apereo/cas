If you wish to directly and separately retrieve attributes from a Groovy script,
the following settings are then relevant:

```properties
# cas.authn.attribute-repository.groovy[0].location=file:/etc/cas/attributes.groovy
# cas.authn.attribute-repository.groovy[0].case-insensitive=false
# cas.authn.attribute-repository.groovy[0].order=0
# cas.authn.attribute-repository.groovy[0].id=
```

The Groovy script may be designed as:

```groovy
import java.util.*

def Map<String, List<Object>> run(final Object... args) {
    def username = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def appContext = args[4]

    logger.debug("[{}]: The received uid is [{}]", this.class.simpleName, uid)
    return[username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
```
