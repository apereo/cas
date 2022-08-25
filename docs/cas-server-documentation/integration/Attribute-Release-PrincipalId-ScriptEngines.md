---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Scripted Principal Id

<div class="alert alert-warning"><strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

Let an external Javascript, Groovy or Python script decide how the principal id attribute should be determined.
This approach takes advantage of scripting functionality built into the Java platform.
While Javascript and Groovy should be natively supported by CAS, Python scripts may need
to massage the CAS configuration to include the [Python modules](https://search.maven.org/search?q=a:jython-standalone).

Scripts will receive and have access to the following variable bindings:

- `id`: The existing identifier for the authenticated principal.
- `attributes`: A map of attributes currently resolved for the principal.
- `logger`: A logger object, able to provide `logger.info()` operations, etc.


```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 500,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.ScriptedRegisteredServiceUsernameProvider",
    "script" : "file:///etc/cas/sampleService.[groovy|js|.py]",
    "canonicalizationMode" : "UPPER"
  }
}
```

Sample Groovy script follows:

```groovy
def run(Object[] args) {
    def attributes = args[0]
    def id = args[1]
    def logger = args[2]
    logger.info("Testing username attribute")
    return "test"
}
```

Sample Javascript function follows:

```javascript
function run(uid, logger) {
   return "test"
}
```
