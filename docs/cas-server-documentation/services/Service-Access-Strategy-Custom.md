---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Custom

If you wish to create your own access strategy and authorization policy enforcer, you will need to
design a component and register it with CAS to handle the enforcement:

```java
package org.apereo.cas;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RegisteredServiceAccessStrategyEnforcer myEnforcer() {
        return new MyRegisteredServiceAccessStrategyEnforcer();
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
