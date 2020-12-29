---
layout: default
title: CAS Properties
category: Configuration
---

{% include variables.html %}

## Authentication Attributes

Set of authentication attributes that are retrieved by the principal resolution process,
typically via some component of [Person Directory](../integration/Attribute-Resolution.html)
from a number of attribute sources unless noted otherwise by the specific authentication scheme.

If multiple attribute repository sources are defined, they are added into a list
and their results are cached and merged.

```properties
# cas.authn.attribute-repository.expiration-time=30
# cas.authn.attribute-repository.expiration-time-unit=MINUTES
# cas.authn.attribute-repository.maximum-cache-size=10000
# cas.authn.attribute-repository.merger=REPLACE|ADD|MULTIVALUED|NONE
# cas.authn.attribute-repository.aggregation=MERGE|CASCADE
```

<div class="alert alert-info"><strong>Remember This</strong><p>Note that in certain cases,
CAS authentication is able to retrieve and resolve attributes from the authentication source in the same authentication request, which would
eliminate the need for configuring a separate attribute repository specially if both the authentication and the attribute source are the same.
Using separate repositories should be required when sources are different, or when there is a need to tackle more advanced attribute
resolution use cases such as cascading, merging, etc.
<a href="../installation/Configuring-Principal-Resolution.html">See this guide</a> for more info.</p></div>

Attributes for all sources are defined in their own individual block.
CAS does not care about the source owner of attributes. It finds them where they can be found and otherwise, it moves on.
This means that certain number of attributes can be resolved via one source and the remaining attributes
may be resolved via another. If there are commonalities across sources, the merger shall decide the final result and behavior.

The story in plain english is:

- I have a bunch of attributes that I wish to resolve for the authenticated principal.
- I have a bunch of sources from which said attributes are retrieved.
- Figure it out.

Note that attribute repository sources, if/when defined, execute in a specific order.
This is important to take into account when attribute merging may take place.
By default, the execution order (when defined) is the following but can be adjusted per source:

1. LDAP
2. JDBC
3. JSON
4. Groovy
5. [Internet2 Grouper](http://www.internet2.edu/products-services/trust-identity/grouper/)
6. REST
7. Script
8. Stubbed/Static

Note that if no *explicit* attribute mappings are defined, all permitted attributes on the record
may be retrieved by CAS from the attribute repository source and made available to the principal. On the other hand,
if explicit attribute mappings are defined, then *only mapped attributes* are retrieved.

### Multimapped Attribute

Attributes may be allowed to be virtually renamed and remapped. The following definition, for instance, attempts to 
grab the attribute `uid` from the attribute source and rename it to `userId`:

```properties
# cas.authn.attribute-repository.[type-placeholder].attributes.uid=userId
```

### Merging Strategies

The following merging strategies can be used to resolve conflicts when the same attribute are found from multiple sources:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `REPLACE`               | Overwrites existing attribute values, if any.
| `ADD`                   | Retains existing attribute values if any, and ignores values from subsequent sources in the resolution chain.
| `MULTIVALUED`           | Combines all values into a single attribute, essentially creating a multi-valued attribute.
| `NONE`                  | Do not merge attributes, only use attributes retrieved during authentication.

### Aggregation Strategies

The following aggregation strategies can be used to resolve and merge attributes
when multiple attribute repository sources are defined to fetch data:
  
| Type            | Description
|-----------------|----------------------------------------------------------------------------------------------------
| `MERGE`         | Default. Query multiple repositories in order and merge the results into a single result set.
| `CASCADE`       | Same as above; results from each query are passed down to the next attribute repository source.

### Stub

Static attributes that need to be mapped to a hardcoded value belong here.

```properties
# cas.authn.attribute-repository.stub.id=

# cas.authn.attribute-repository.stub.attributes.uid=uid
# cas.authn.attribute-repository.stub.attributes.displayName=displayName
# cas.authn.attribute-repository.stub.attributes.cn=commonName
# cas.authn.attribute-repository.stub.attributes.affiliation=groupMembership
```

### LDAP

{% include {{ version }}/ldap-configuration.md configKey="cas.authn.attribute-repository.ldap[0]" %}

```properties
# cas.authn.attribute-repository.ldap[0].id=
# cas.authn.attribute-repository.ldap[0].order=0

# cas.authn.attribute-repository.ldap[0].attributes.uid=uid
# cas.authn.attribute-repository.ldap[0].attributes.display-name=displayName
# cas.authn.attribute-repository.ldap[0].attributes.cn=commonName
# cas.authn.attribute-repository.ldap[0].attributes.affiliation=groupMembership
```

To fetch and resolve attributes that carry tags/options, consider tagging the mapped attribute as such:

```properties
# cas.authn.attribute-repository.ldap[0].attributes.affiliation=affiliation;
```
                                  
### Groovy

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

### JSON

If you wish to directly and separately retrieve attributes from a static JSON source,
the following settings are then relevant:

```properties
# cas.authn.attribute-repository.json[0].location=file://etc/cas/attribute-repository.json
# cas.authn.attribute-repository.json[0].order=0
# cas.authn.attribute-repository.json[0].id=
```

The format of the file may be:

```json
{
    "user1": {
        "firstName":["Json1"],
        "lastName":["One"]
    },
    "user2": {
        "firstName":["Json2"],
        "eduPersonAffiliation":["employee", "student"]
    }
}
```

### REST

{% include {{ version }}/rest-integration.md configKey="cas.authn.attribute-repository.rest[0]" %}

```properties
# cas.authn.attribute-repository.rest[0].order=0
# cas.authn.attribute-repository.rest[0].id=
# cas.authn.attribute-repository.rest[0].case-insensitive=false
```

The authenticating user id is passed in form of a request parameter under `username`. The response is expected
to be a JSON map as such:

```json
{
  "name" : "JohnSmith",
  "age" : 29,
  "messages": ["msg 1", "msg 2", "msg 3"]
}
```

### Python/Javascript/Groovy

<div class="alert alert-warning"><strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

Similar to the Groovy option but more versatile, this option takes advantage of Java's native 
scripting API to invoke Groovy, Python or Javascript scripting engines to compile a pre-defined script to resolve attributes. 
The following settings are relevant:

```properties
# cas.authn.attribute-repository.script[0].location=file:/etc/cas/script.groovy
# cas.authn.attribute-repository.script[0].order=0
# cas.authn.attribute-repository.script[0].id=
# cas.authn.attribute-repository.script[0].case-insensitive=false
# cas.authn.attribute-repository.script[0].engine-name=js|groovy|python
```

While Javascript and Groovy should be natively supported by CAS, Python scripts may need
to massage the CAS configuration to include the [Python modules](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jython-standalone%22).

The Groovy script may be defined as:

```groovy
import java.util.*

Map<String, List<Object>> run(final Object... args) {
    def uid = args[0]
    def logger = args[1]

    logger.debug("Groovy things are happening just fine with UID: {}",uid)
    return[username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
```

The Javascript script may be defined as:

```javascript
function run(uid, logger) {
    print("Things are happening just fine")
    logger.warn("Javascript called with UID: {}",uid);

    // If you want to call back into Java, this is one way to do so
    var javaObj = new JavaImporter(org.yourorgname.yourpackagename);
    with (javaObj) {
        var objFromJava = JavaClassInPackage.someStaticMethod(uid);
    }

    var map = {};
    map["attr_from_java"] = objFromJava.getSomething();
    map["username"] = uid;
    map["likes"] = "cheese";
    map["id"] = [1234,2,3,4,5];
    map["another"] = "attribute";

    return map;
}
```

### JDBC

Retrieve attributes from a JDBC source. 

{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.attribute-repository.jdbc[0]" %}

```properties
# cas.authn.attribute-repository.jdbc[0].attributes.uid=uid
# cas.authn.attribute-repository.jdbc[0].attributes.display-name=displayName
# cas.authn.attribute-repository.jdbc[0].attributes.cn=commonName
# cas.authn.attribute-repository.jdbc[0].attributes.affiliation=groupMembership

# cas.authn.attribute-repository.jdbc[0].single-row=true
# cas.authn.attribute-repository.jdbc[0].order=0
# cas.authn.attribute-repository.jdbc[0].id=
# cas.authn.attribute-repository.jdbc[0].require-all-attributes=true
# cas.authn.attribute-repository.jdbc[0].case-canonicalization=NONE|LOWER|UPPER
# cas.authn.attribute-repository.jdbc[0].query-type=OR|AND
# cas.authn.attribute-repository.jdbc[0].case-insensitive-query-attributes=username

# Used only when there is a mapping of many rows to one user
# cas.authn.attribute-repository.jdbc[0].column-mappings.column-attr-name1=columnAttrValue1
# cas.authn.attribute-repository.jdbc[0].column-mappings.column-attr-name2=columnAttrValue2
# cas.authn.attribute-repository.jdbc[0].column-mappings.column-attr-name3=columnAttrValue3

# cas.authn.attribute-repository.jdbc[0].sql=SELECT * FROM table WHERE {0}
# cas.authn.attribute-repository.jdbc[0].username=uid
```

### Grouper

This option reads all the groups from [a Grouper instance](https://incommon.org/software/grouper/) for the given CAS principal and adopts them as CAS attributes under a `grouperGroups` multi-valued attribute.
To learn more about this topic, [please review this guide](../integration/Attribute-Resolution.html).

```properties
# cas.authn.attribute-repository.grouper[0].enabled=true
# cas.authn.attribute-repository.grouper[0].id=
# cas.authn.attribute-repository.grouper[0].order=0
```

You will also need to ensure `grouper.client.properties` is available on the classpath (i.e. `src/main/resources`)
with the following configured properties:

```properties
# grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
# grouperClient.webService.login = banderson
# grouperClient.webService.password = password
```

### Couchbase

This option will fetch attributes from a Couchbase database for a given CAS principal. To 
learn more about this topic, [please review this guide](../installation/Couchbase-Authentication.html). 

{% include {{ version }}/couchbase-configuration.md configKey="cas.authn.attribute-repository.couchbase" %}

```properties
# cas.authn.attribute-repository.couchbase.usernameAttribute=username
# cas.authn.attribute-repository.couchbase.order=0
# cas.authn.attribute-repository.couchbase.id=
```

### Redis

This option will fetch attributes from a Redis database for a given CAS principal. 

To learn more about this topic, [please review this guide](../installation/Redis-Authentication.html).

{% include {{ version }}/redis-configuration.md configKey="cas.authn.attribute-repository" %}

```properties
# cas.authn.attribute-repository.redis.order=0
# cas.authn.attribute-repository.redis.id=
```

### Microsoft Azure Active Directory

This option will fetch attributes from Microsoft Azure Active Directory using the Microsoft Graph API.

The following settings are available:

```properties
# cas.authn.attribute-repository.azure-active-directory[0].client-id=
# cas.authn.attribute-repository.azure-active-directory[0].client-secret=
# cas.authn.attribute-repository.azure-active-directory[0].client-secret=
# cas.authn.attribute-repository.azure-active-directory[0].tenant=

# cas.authn.attribute-repository.azure-active-directory[0].id=
# cas.authn.attribute-repository.azure-active-directory[0].order=0
# cas.authn.attribute-repository.azure-active-directory[0].case-insensitive=false
# cas.authn.attribute-repository.azure-active-directory[0].resource=
# cas.authn.attribute-repository.azure-active-directory[0].scope=
# cas.authn.attribute-repository.azure-active-directory[0].grant-type=
# cas.authn.attribute-repository.azure-active-directory[0].api-base-url=
# cas.authn.attribute-repository.azure-active-directory[0].attributes=
# cas.authn.attribute-repository.azure-active-directory[0].domain=
# cas.authn.attribute-repository.azure-active-directory[0].logging-level=
```

### Default Bundle

If you wish to release a default bundle of attributes to all applications,
and you would rather not duplicate the same attribute per every service definition,
then the following settings are relevant:

```properties
# cas.authn.attribute-repository.default-attributes-to-release=cn,givenName,uid,affiliation
```

To learn more about this topic, [please review this guide](../integration/Attribute-Release.html).

### Protocol Attributes

Defines whether CAS should include and release protocol attributes defined in the specification in addition to the
principal attributes. By default all authentication attributes are released when protocol attributes are enabled for
release. If you wish to restrict which authentication attributes get released, you can use the below settings to control authentication attributes more globally.

Protocol/authentication attributes may also be released conditionally on a per-service 
basis. To learn more about this topic, [please review this guide](../integration/Attribute-Release.html).

```properties
# cas.authn.authentication-attribute-release.only-release=authenticationDate,isFromNewLogin
# cas.authn.authentication-attribute-release.never-release=
# cas.authn.authentication-attribute-release.enabled=true
```
