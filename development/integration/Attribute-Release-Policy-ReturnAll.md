# Attribute Release Policy - Return All

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Policy-ReturnAll.html

Return all resolved principal attributes to the service, and optionally exclude attributes from the final collection.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllAttributeReleasePolicy",
    "excludedAttributes": ["java.util.LinkedHashSet", ["cn"]]
  }
}
```
