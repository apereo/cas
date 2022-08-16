---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Pattern Matching

This policy allows the release of defined allowed attributes only if the attrribute value(s)
matches the given regular expression pattern. If the attribute value is matched successfully, the policy
is then able to apply transformation rules on the value to extract
and collect the *matched groups* to then assemble the final attribute value.

For example, consider an authenticated principal with a `memberOf` attribute
which contains values such as `CN=g1,OU=example,DC=org`, and `CN=g2,OU=example,DC=org`. The following policy
applies the defined pattern and the transformation on each attribute value. The final result would be a `memberOf`
attribute with values `g1@example.org` and `g2@example.org`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
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


