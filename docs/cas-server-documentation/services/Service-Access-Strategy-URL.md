---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Unauthorized URL


The default strategy allows one to configure a service with the following properties:

| Field                     | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `unauthorizedRedirectUrl` | Optional url to redirect the flow in case service access is not allowed.                                                                                                                                                                                                                                                                                                                                                                                                        |


Service access is denied if the principal does *not* have a `cn` attribute containing the value `super-user`.
If so, the user will be redirected to `https://www.github.com` instead.

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id": 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "unauthorizedRedirectUrl" : "https://www.github.com",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "super-user" ] ]
    }
  }
}
```

## Dynamic URLs

Service access is denied if the principal does *not* have a `cn` attribute containing the
value `super-user`. If so, the redirect URL will be dynamically determined based 
on outcome of the specified Groovy script.

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id": 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "unauthorizedRedirectUrl" : "file:/etc/cas/config/unauthz-redirect-url.groovy",
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "super-user" ] ]
    }
  }
}
```

The script itself will take the following form:

```groovy
import org.apereo.cas.*
import org.apereo.cas.web.support.*
import java.util.*
import java.net.*
import org.apereo.cas.authentication.*

URI run(final Object... args) {
    def registeredService = args[0]
    def requestContext = args[1]
    def applicationContext = args[2]
    def logger = args[3]
    
    logger.info("Redirecting to somewhere, processing [{}]", registeredService.name)
    /**
     * Stuff Happens...
     */
    return new URI("https://www.github.com");
}
```

The following parameters are provided to the script:

| Field                | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `registeredService`  | The object representing the matching registered service in the registry.    |
| `requestContext`     | The object representing the Spring Webflow `RequestContext`.                |
| `applicationContext` | The object representing the Spring `ApplicationContext`.                    |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`. |
