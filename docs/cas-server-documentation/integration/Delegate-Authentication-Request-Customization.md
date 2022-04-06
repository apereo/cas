---
layout: default
title: CAS - Delegated Authentication Request Customization
category: Authentication
---

{% include variables.html %}

# Delegated Authentication Request Customization

Authentication (HTTP) requests that are sent from CAS to configured identity providers are can be customized at runtime. This customization phase of the authentication request happens right before the request is constructed and passed onto the client browser. Before the customizer proceeds, it also must declare its support for the given identity provider and/or request, and it also is given the chance to determine if the identity provider is authorized for the given request.

## Groovy Customization

The authentication request can be customized using an external Groovy script. 

{% include_cached casproperties.html properties="cas.authn.pac4j.core.groovy-authentication-request-customizer" %}

The outline of the Groovy script would be as follows:
                                                         
```groovy
import org.apereo.cas.authentication.principal.*
import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.pac4j.core.client.*
import org.pac4j.core.context.*
import org.springframework.context.*

def run(Object[] args) {
    def client = args[0] as IndirectClient
    def webContext = args[1] as WebContext
    def appContext = args[2] as ApplicationContext
    def logger = args[3]
    logger.info("Checking ${client.name}...")
    webContext.setRequestAttribute("customAttribute", "value")
}

def supports(Object[] args) {
    def client = args[0] as IndirectClient
    def webContext = args[1] as WebContext
    def appContext = args[2] as ApplicationContext
    def logger = args[3]
    logger.info("Checking support for ${client.name}...")
    return true
}

def isAuthorized(Object[] args) {
    def client = args[0] as IndirectClient
    def webContext = args[1] as WebContext
    def service = args[2] as WebApplicationService
    def appContext = args[3] as ApplicationContext
    def logger = args[4]
    logger.info("Checking authorization for ${client.name}...")
    return true
}
```

The parameters passed are as follows:

| Parameter    | Description                                                                 |
|--------------|-----------------------------------------------------------------------------|
| `client`     | The object representing the identity provider as `IndirectClient`.          |
| `webContext` | The object representing the HTTP request/response as `WebContext`.          |
| `service`    | The `WebApplicationService` for the incoming application request.           |
| `appContext` | The Spring `ApplicationContext`.                                            |
| `logger`     | The object responsible for issuing log messages such as `logger.info(...)`. |

  
## Custom

For more advanced scenarios, you can also design your webflow exception handler
and register it with the CAS at runtime:

```java
@Bean
public DelegatedClientAuthenticationRequestCustomizer myCustomizer() {
    return new DelegatedClientAuthenticationRequestCustomizer();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more
about how to register configurations into the CAS runtime.
