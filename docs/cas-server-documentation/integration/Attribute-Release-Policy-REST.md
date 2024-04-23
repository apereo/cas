---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - REST

Only return the principal attributes that are explicitly allowed by contacting a REST endpoint. Endpoints must be designed to
accept/process `application/json` and must be able to respond to a `GET` request. The expected response status code is `200` where the body of
the response includes a `Map` of attributes linked to their values.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnRestfulAttributeReleasePolicy",
    "endpoint" : "https://somewhere.example.org",
    "headers": {
      "@class": "java.util.LinkedHashMap",
      "header": "value"
    }
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "cn" : "commonName"
    }
  }
}
```

The following parameters are passed to the endpoint:

| Parameter   | Description                                                                   |
|-------------|-------------------------------------------------------------------------------|
| `principal` | The object representing the authenticated principal.                          |
| `service`   | The object representing the corresponding service definition in the registry. |

The body of the submitted request may also include a `Map` of currently resolved attributes.

The `allowedAttributes` field is an optional attribute that allows the policy to remap attributes virtually.
If the attribute is undefined or empty, all received attributes will be considered authorized for release on
an as-is basis. If attribute mapping rules are defined, received attributes are filtered through the mapping rules
and the results would be allowed for release.

The range of supported mapping rules and options are the same as those supported by the *Return Mapped* policy in its various forms.
For example, the above configuration will accept a `cn` attribute from the external REST endpoint and will virtually rename
that attribute into `commonName` instead.

