# Static Principal Id

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-PrincipalId-Static.html

Returns a static, hardcoded value for the username attribute. Values can use the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.StaticRegisteredServiceUsernameProvider",
    "value": "always-static-username"
  }
}
```

You may be able to carry out the same task with [Groovy](https://apereo.github.io/cas/development/integration/Attribute-Release-PrincipalId-Groovy.html), though this option here is slightly more efficient performance-wise.
