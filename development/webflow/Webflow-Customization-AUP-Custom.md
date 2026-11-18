# Custom Acceptable Usage Policy

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-Custom.html

Keep acceptable usage policy acceptance in your own store by registering a custom `AcceptableUsagePolicyRepository` with CAS.

If you wish to design your own storage mechanism, you may follow the below approach:

```java
package org.apereo.cas.custom;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyUsagePolicyConfiguration {

    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
      ...
    }

}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
