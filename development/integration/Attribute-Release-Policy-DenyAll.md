# Attribute Release Policy - Deny All

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Policy-DenyAll.html

Never ever return principal attributes to applications. Note that this policy also skips and refuses to release default attributes, if any.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.DenyAllAttributeReleasePolicy"
  }
}
```
