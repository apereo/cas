---
layout: default
title: Themes - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# Groovy Themes - User Interface Customization

If you have multiple themes defined, it may be desirable to dynamically determine a theme for a given service definition. In
order to do so, you may calculate the final theme name via a Groovy script of your own design. The theme assigned to
the service definition needs to point to the location of the script:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://www.example.org",
  "name" : "MyTheme",
  "theme" : "file:///etc/cas/config/themes.groovy",
  "id" : 1000
}
```

The script itself may be designed as:

```groovy
import java.util.*

def run(final Object... args) {
    def (service,registeredService,queryStrings,headers,logger) = args

    // Determine theme ...

    return null
}
```

Returning `null` or blank will have CAS switch to the default theme. The following parameters may be passed to a Groovy script:

| Parameter           | Description                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| `service`           | The object representing the requesting service.                             |
| `registeredService` | The object representing the matching registered service in the registry.    |
| `queryStrings`      | Textual representation of all query strings found in the request, if any.   |
| `headers`           | `Map` of all request headers and their values found in the request, if any. |
| `logger`            | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
