---
layout: default
title: CAS - Configuring Authentication Policy
category: Authentication
---
{% include variables.html %}

# Global - Authentication Policy

Global authentication policy that is applied when CAS attempts to vend and validate tickets.

{% include_cached casproperties.html properties="cas.authn.policy.required-handler-authentication-policy-enabled" %}

If you wish to design your own global authentication policy,
you may define the following bean definition in your environment:

```java
@AutoConfiguration
public class MyConfiguration {
    @Bean
    public AuthenticationPolicy globalAuthenticationPolicy() {
        return new MyAuthenticationPolicy();
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
