---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Inline Groovy

Principal attributes that are mapped may produce their values from an inline groovy script. As an example, if you currently
have resolved a `uid` attribute with a value of `piper`, you could then consider the following:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "uid" : "groovy { return attributes['uid'].get(0) + ' is great' }"
    }
  }
}
```

In the above snippet, the value of the `uid` attribute name is mapped to the result of the inline groovy script.
Inline scripts always begin with the syntax `groovy {...}` and are passed the current collection of resolved
attributes as an `attributes` binding variable. The result of the script can be a single/collection of value(s).

<div class="alert alert-info"><strong>Usage Warning</strong><p>Activating this policy is not without cost,
as CAS needs to evaluate the inline script, compile and run it for subsequent executions. While the compiled
script is cached and should help with execution performance, as a general rule, you should avoid opting
for and designing complicated scripts.</p></div>

The above configuration will produce a `uid` attribute for the application whose value is a concatenation of
the original value of `uid` plus the words " is great", so the final result would be "piper is great".

