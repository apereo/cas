---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Pattern Matching

This policy allows the release of defined allowed attributes only if the attribute value(s)
matches the given regular expression pattern. If the attribute value is matched successfully, the policy
is then able to apply transformation rules on the value to extract
and collect the *matched groups* to then assemble the final attribute value.

{% tabs transformationrules %}

{% tab transformationrules Pattern Extraction %}

Consider an authenticated principal with a `memberOf` attribute
which contains values such as `CN=g1,OU=example,DC=org`, and `CN=g2,OU=example,DC=org`. The following policy
applies the defined pattern and the transformation on each attribute value. The final result would be a `memberOf`
attribute with values `g1@example.org` and `g2@example.org`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class": "org.apereo.cas.services.PatternMatchingAttributeReleasePolicy",
    "allowedAttributes": {
        "@class": "java.util.TreeMap",
        "memberOf": {
            "@class": "org.apereo.cas.services.PatternMatchingAttributeReleasePolicy$Rule",
            "pattern": "^CN=(\\w+),\\s*OU=(\\w+),\\s*DC=(\\w+)",
            "transform": "${1}@${2}/${3}"
        }
    }
  }
}
```

Matched pattern groups typically start at `1`. If you need to refer to the entire matched region, use `${0}`.

{% endtab %}

{% tab transformationrules Inline Groovy %}

Consider an authenticated principal with a `memberOf` attribute
which contains values such as `CN=g1,OU=example,DC=org`, and `CN=g2,OU=example,DC=org`. The following policy
applies the defined pattern and the transformation on each attribute value. 

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class": "org.apereo.cas.services.PatternMatchingAttributeReleasePolicy",
    "allowedAttributes": {
        "@class": "java.util.TreeMap",
        "memberOf": {
          "@class": "org.apereo.cas.services.PatternMatchingAttributeReleasePolicy$Rule",
          "pattern": ".*CN=(\w+),OU=example.*",
          "transform":
            '''
                groovy {
                    logger.info("Full matched value is ${matched}")
                    def value = matchedGroup1 == 'g1' ? 'group1' : 'group2'
                    // return a Map. Note the map value must be a collection
                    return [mem: [value]]
                }
            '''
        }
    }
  }
}
```
     
The final released attribute would be `mem` with values `group1` and `group2`.

The following parameters are passed to the script:

| Parameter       | Description                                                                                        |
|-----------------|----------------------------------------------------------------------------------------------------|
| `attributes`    | `Map` of attributes currently resolved and available for release.                                  |
| `logger`        | The object responsible for issuing log messages such as `logger.info(...)`.                        |
| `context`       | The object representing the attribute release policy context carrying `service`, `principal`, etc. |
| `matched`       | The full matched value.                                                                            |
| `matchedGroup0` | Same as `matched`.                                                                                 |
| `matchedGroupX` | Matched group where `X` is the group index, if the pattern uses groups.                            |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% endtabs %}




