---
layout: default
title: CAS - Principal Attribute Per Application - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Principal Attribute Per Application - Multifactor Authentication Triggers

As a hybrid option, MFA can be triggered for a specific application registered inside the CAS service registry, provided
the authenticated principal carries an attribute that matches a configured attribute value. The attribute
value can be an arbitrary regex pattern. See below to learn about how to configure MFA settings.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "name": "test",
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ],
    "principalAttributeNameTrigger" : "memberOf",
    "principalAttributeValueToMatch" : "faculty|allMfaMembers"
  }
}
```

The attribute name may list several attributes separated by commas. If more than one provider is listed for the
application, the [provider selection strategy](Multifactor-Authentication-ProviderSelection.html) decides which
one is activated once the attribute value matches.

## Provider Selection Mappings

When different users of the same application should go to different providers, the application can map each provider
to its own attribute value pattern instead. CAS reads the values of the attribute(s) named by `principalAttributeNameTrigger`
and activates the first provider whose pattern matches one of them, without a separate provider selection step:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "name": "test",
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "principalAttributeNameTrigger" : "memberOf",
    "multifactorProviderSelectionMappings" : {
      "@class" : "java.util.LinkedHashMap",
      "mfa-duo" : "(?i)^faculty$",
      "mfa-gauth" : "(?i)^(staff|student)$"
    }
  }
}
```

- Keys are provider ids and values are regular expressions, matched against each value of the attribute. A pattern
  matches when it is found anywhere in a value, so anchor it with `^` and `$` to require the whole value.
- Mappings are evaluated in the order they are defined, so use a `java.util.LinkedHashMap`: when several patterns match,
  the first one wins. A mapping whose provider is not available in CAS is skipped.

