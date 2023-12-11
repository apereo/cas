---
layout: default
title: CAS - Scripting with Apache Groovy
category: Installation
---
{% include variables.html %}

# Scripting with Apache Groovy

[Apache Groovy](https://groovy-lang.org/) is a powerful, optionally typed and dynamic language, with static-typing and static compilation capabilities, 
for the Java platform aimed at improving developer productivity thanks to a concise, familiar and easy to learn syntax. CAS takes advantage of Groovy in 
forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more.

As an example, the following construct is what's referred to in CAS as an *embedded Groovy script**:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://example.app.org/login",
  "name": "Sample",
  "id": 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "name" : "groovy { return ['casuser'] }"
    }
  }
}
```

By default, all such scripts are evaluated and executed *dynamically* using Groovy meta object protocol. There is also support for the
alternative that allows CAS to tune the Groovy compiler for static compilation. In this mode, all methods, properties, files, 
inner classes, etc. found in scripts will be type checked. If you wish to always compile Groovy scripts using `CompileStatic`, 
you may specify the following **system property** when you run CAS:

```bash
-Dorg.apereo.cas.groovy.compile.static=true
```

When CAS runs in `CompileStatic` mode, Groovy scripts most likely will need to be rewritten to remove all dynamic constructs.
For example, the following Groovy script is one that uses dynamic/meta aspects of the Groovy programming language:

```groovy
if (attributes['entitlement'].contains('admin')) {
    return [attributes['uid'].get(0).toUpperCase()]
} else {
    return attributes['identifier']
}
```
 
The same script in `CompileStatic` mode would be rewritten as:

```groovy
def attributes = (Map) binding.getVariable('attributes')
if ((attributes.get('entitlement') as List).contains('admin')) {
    return [(attributes['uid'] as List).get(0).toString().toUpperCase()]
} else {
    return attributes['identifier'] as List
}
```
