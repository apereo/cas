---
layout: default
title: CAS - Web Flow Customization
category: Webflow Management
---

{% include variables.html %}

# Webflow Errors Customization

By default CAS is configured to recognize and handle a number of exceptions for web flow during authentication. Each exception 
has the specific message bundle mapping in `messages.properties` So that a specific message could be presented to end users
on the login form. Any un-recognized or un-mapped exceptions results in the `UNKNOWN` mapping with a generic `Invalid credentials.` message.

To map custom exceptions in the webflow, one would need map the exception in CAS 
settings and then define the relevant error in `messages.properties`:

```properties
authenticationFailure.MyAuthenticationException=Authentication has failed, but it did it my way!
```

{% include_cached casproperties.html properties="cas.authn.errors" %}

## Groovy Error Handling

Exceptions and authentication errors that are reported back to the webflow may be 
caught and processed using an external Groovy script. If the script can support and handle the error,
its outcome should be an event that the webflow should use as the final destination state:

The outline of the script should match the following:

```groovy
import org.apereo.cas.*
import org.springframework.context.*
import org.springframework.webflow.*
import org.springframework.webflow.action.*
import org.springframework.webflow.execution.*

def run(Object[] args) {
    def exception = args[0] as Exception
    def requestContext = args[1] as RequestContext
    def applicationContext = args[2] as ApplicationContext
    def logger = args[3]

    logger.info("Handling {}", exception)
    new EventFactorySupport().event(this, "customEvent")
}

def supports(Object[] args) {
    def exception = args[0] as Exception
    def requestContext = args[1] as RequestContext
    def applicationContext = args[2] as ApplicationContext
    def logger = args[3]

    logger.info("Checking to support {}", exception)
    true
}
```

The parameters passed are as follows:

| Parameter            | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `exception`          | The object representing the error.                                          |
| `requestContext`     | The object representing the Spring Webflow `RequestContext`.                |
| `applicationContext` | The object representing the Spring `ApplicationContext`.                    |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`. |

## Custom Error Handling
     
For more advanced scenarios, you can also design your webflow exception handler
and register it with the CAS at runtime:

```java
@Bean
public CasWebflowExceptionHandler customExceptionHandler(){
    return new CustomCasWebflowExceptionHandler();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more
about how to register configurations into the CAS runtime.
