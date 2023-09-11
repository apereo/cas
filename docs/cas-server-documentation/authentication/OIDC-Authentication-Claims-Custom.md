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
