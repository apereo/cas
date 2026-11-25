# Attribute Release Policy - Return Static

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Policy-ReturnStatic.html

Return static attributes to the service with values hard-coded in the service definition.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class": "org.apereo.cas.services.ReturnStaticAttributeReleasePolicy",
    "allowedAttributes": {
      "@class": "java.util.LinkedHashMap",
      "permissions": [ "java.util.ArrayList", [ "read", "write", "admin" ] ]
    }
  }
}
```

Attribute values can use the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.
