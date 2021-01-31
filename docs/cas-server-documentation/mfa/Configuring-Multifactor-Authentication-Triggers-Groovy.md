---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Groovy - Multifactor Authentication Triggers

MFA can be triggered based on the results of a groovy script of your own design. The 
outcome of the script should determine the MFA provider id that CAS should attempt to activate.

The outline of the groovy script is shown below as a sample:

```groovy
import java.util.*

class SampleGroovyEventResolver {
    def String run(final Object... args) {
        def service = args[0]
        def registeredService = args[1]
        def authentication = args[2]
        def httpRequest = args[3]
        def logger = args[4]

        ...

        return "mfa-duo"
    }
}
```

The parameters passed are as follows:

| Parameter             | Description
|-----------------------|-----------------------------------------------------------------------
| `service`             | The object representing the incoming service provided in the request, if any.
| `registeredService`   | The object representing the corresponding service definition in the registry.
| `authentication`      | The object representing the established authentication event, containing the principal.
| `httpRequest`         | The object representing the `HttpServletRequest`.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.

As an example, the following script triggers multifactor authentication 
via [Duo Security](../mfa/DuoSecurity-Authentication.html), if the requesting application is `https://www.example.com` and 
the authenticated principal contains a `mail` attribute whose values contain `email@example.org`.

```groovy
import java.util.*

class MyExampleScript {
    String run(final Object... args) {
        def service = args[0]
        def registeredService = args[1]
        def authentication = args[2]
        def httpRequest = args[3]
        def logger = args[4]

        if (service.id == "https://www.example.com") {
            logger.info("Evaluating principal attributes [{}]", authentication.principal.attributes)

            def mail = authentication.principal.attributes['mail']
            if (mail.contains("email@example.org")) {
                logger.info("Found mail attribute with value [{}]", mail)
                return "mfa-duo"
            }
        }
        return null
    }
}
```

{% include casproperties.html properties="cas.authn.mfa.groovy-script" %}
