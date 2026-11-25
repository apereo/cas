# Service Access Strategy - Custom

Source: https://apereo.github.io/cas/development/services/Service-Access-Strategy-Custom.html

Enforce your own authorization rules by implementing a `RegisteredServiceAccessStrategyEnforcer` and registering it with CAS.

If you wish to create your own access strategy and authorization policy enforcer, you will need to design a component and register it with CAS to handle the enforcement:

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

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
