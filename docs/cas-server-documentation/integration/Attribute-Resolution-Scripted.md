---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Groovy Attribute Resolution

The following configuration describes how to fetch and retrieve attributes from Scripted attribute repositories.

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

Similar to the Groovy option but more versatile, this option takes advantage of Java's native
scripting API to invoke Groovy, Python or Javascript scripting engines to compile a pre-defined script to resolve attributes.
The following settings are relevant:

{% include_cached casproperties.html properties="cas.authn.attribute-repository.script" %}

The Groovy script may be defined as:

```groovy
import java.util.*

Map<String, List<Object>> run(final Object... args) {
    def (uid,logger) = args
    logger.debug("Groovy things are happening just fine with UID: {}",uid)
    return [username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
