---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Script Engines

<div class="alert alert-warning"><strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

Use alternative script engine implementations and other programming languages to configure attribute release policies. This approach
takes advantage of scripting functionality built into the Java platform via additional libraries and drivers. While Groovy should be
natively supported by CAS, the following module is required in the overlay to include support for additional languages
such as Python, etc.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-script-engines" %}

The service definition then may be designed as:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy",
    "scriptFile" : "classpath:/script.[py|js|groovy]"
  }
}
```

The configuration of this component qualifies to use
the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. The scripts
need to design a `run` function that receives a list of parameters. The collection of current attributes in process
as well as a logger object are passed to this function. The result must produce a map whose `key`s are attributes names
and whose `value`s are a list of attribute values.

As an example, the script itself may be designed in Groovy as:

```groovy
import java.util.*

def Map<String, List<Object>> run(final Object... args) {
    def currentAttributes = args[0]
    def logger = args[1]

    logger.debug("Current attributes received are {}", currentAttributes)
    return[username:["something"], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
```

Here's the same script written in Python:

```python
def run(*Params):
  Attributes = Params[0]
  Logger = Params[1]
  # Calculate attributes and return a new dictionary of attributes...
  return ...
```

You are also allowed to stuff inlined groovy scripts into the `scriptFile` attribute. The script
has access to the collection of resolved `attributes` as well as a `logger` object.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy",
    "scriptFile" : "groovy { return attributes }"
  }
}
```

