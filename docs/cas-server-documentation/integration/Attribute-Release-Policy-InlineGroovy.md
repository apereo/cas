---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Inline Groovy

Principal attributes that are mapped may produce their values from an inline groovy script. 

<div class="alert alert-info">:information_source: <strong>Usage Warning</strong><p>Activating this policy is not without cost,
as CAS needs to evaluate the inline script, compile and run it for subsequent executions. While the compiled
script is cached and should help with execution performance, as a general rule, you should avoid opting
for and designing complicated scripts.</p></div>

{% tabs groovyinline %}

{% tab groovyinline <i class="fa fa-pencil px-1"></i>Basic %}

As an example, if you currently have resolved a `uid` attribute with a value of `piper`, you could then consider the following:

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

The above configuration will produce a `uid` attribute for the application whose value is a concatenation of
the original value of `uid` plus the words `is great`. The final result would be `piper is great`.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab groovyinline <i class="fa fa-gears px-1"></i>Complex %}
              
This is a more complicated example of an inline Groovy script that calculates the value for the `memberOf` attribute. 
You will notice the Groovy script is defined as a multiline string that is directly mapped to the attribute.
     
Given a `memberOf` attribute value of `CN=Colleague Admins,OU=Computer Services Users`, the `memberOf` attribute
that is calculated by the below attribute release policy will produce `Colleague Admins` as its value.

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://app.example.org/.+",
  "id": 1,
  "name": "Sample",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "memberOf" :
          '''
          groovy {
            def value = attributes['memberOf']?.get(0) as String
            println "memberOf attribute: $value"
            if (value != null) {
              def matcher = (value =~ ~/(CN=)(.*?)(?<!\\),.*/)
              if (matcher.find()) {
                def match = matcher.group(2)
                println "Found a match: $match"
                return match
              }
              println "No match found for memberOf. Returning $value"
              return value
            }
            println "No memberOf attribute is found"
            return null
          }
          '''
    }
  }
}
```
 
You may also be interested in doing almost the same thing via [Pattern Matching](Attribute-Release-Policy-PatternMatching.html).

<div class="alert alert-info">:information_source: <strong>Usage Warning</strong><p>As you may note, this can get ugly very quickly
specially if you decide to be super creative with the scripting logic and/or decide to duplicate the same kind of script
throughout other service policy files. Inline Groovy scripts are meant to be brief and efficient and you are encouraged
to not get too complicated with the structure of the script and the behavior it delivers.</p></div>

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% endtabs %}
