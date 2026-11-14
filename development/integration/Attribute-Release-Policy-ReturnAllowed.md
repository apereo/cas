# Attribute Release Policy - Return Allowed

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Policy-ReturnAllowed.html

Only return the principal attributes that are explicitly allowed by the service definition.

**Default**

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ]
  }
}
```

Attributes authorized and allowed for release by this policy may not necessarily be available as resolved principal attributes and can be resolved on the fly dynamically using the [attribute definition store](https://apereo.github.io/cas/development/integration/Attribute-Definitions.html).

**Groovy**

Allowed attributes may also contain inline Groovy script that would be tasked to build attributes dynamically on the fly and return back a `Map<String, List<Object>>` of results:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
  "name" : "Sample",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", 
      [ 
        "cn", 
        "groovy { [ fullName: [ 'FN/' + attributes['fullName'][0] ] ] }", 
        "sn" 
      ] 
    ]
  }
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
