---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Scripted Python/Javascript/Groovy Attribute Resolution

The following configuration describes how to fetch and retrieve attributes from Scripted attribute repositories.

<div class="alert alert-warning"><strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

Similar to the Groovy option but more versatile, this option takes advantage of Java's native
scripting API to invoke Groovy, Python or Javascript scripting engines to compile a pre-defined script to resolve attributes.
The following settings are relevant:

{% include_cached casproperties.html properties="cas.authn.attribute-repository.script" %}

While Javascript and Groovy should be natively supported by CAS, Python scripts may need
to massage the CAS configuration to include the [Python modules](https://search.maven.org/search?q=a:jython-standalone).

The Groovy script may be defined as:

```groovy
import java.util.*

Map<String, List<Object>> run(final Object... args) {
    def uid = args[0]
    def logger = args[1]

    logger.debug("Groovy things are happening just fine with UID: {}",uid)
    return [username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
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
