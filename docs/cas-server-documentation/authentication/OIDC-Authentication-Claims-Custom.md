---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Custom Claims - OpenID Connect Authentication

If you wish to design your own claim assembly strategy and collect claims into an ID token, 
you may define the following bean definition in your environment:

```java
@AutoConfiguration
public class MyOidcConfiguration {
    @Bean
    public OidcIdTokenClaimCollector oidcIdTokenClaimCollector() {
        return new MyIdTokenClaimCollector();
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
          
## Groovy Claims
  
Custom claims can also be collected and assembled using a Groovy script.

{% include_cached casproperties.html properties="cas.authn.oidc.id-token" %}

The script itself may be designed as such:

```groovy
def collect(Object... args) {
    def (claims, name, values, registeredService, applicationContext, logger) = args
    println("Collecting individual claim ${name} for ${registeredService?.getName()}")
    claims.setStringClaim("cn", "CAS User")
}

def conclude(Object... args) {
    def (claims, registeredService, applicationContext, logger) = args
    println("Finalizing all claims for ${registeredService?.getName()}")
    claims.setStringClaim("givenName", "ApereoCAS")
    claims.setStringListClaim("roles", ["admin", "user"])
}
```

The parameters passed are as follows:

| Parameter            | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `claims`             | The `JwtClaims` object representing the ID token claims.                    |
| `name`               | The name of the claim being collected, when passed.                         |
| `values`             | The list of values associated with the claim, when passed.                  |
| `registeredService`  | The object representing the registered service.                             |
| `applicationContext` | The object representing the Spring `ApplicationContext`.                    |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy,
please [review this guide](../integration/Apache-Groovy-Scripting.html).

