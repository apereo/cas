# Custom - Attribute Consent Storage

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Consent-Storage-Custom.html

Keep attribute consent decisions in your own store by registering a custom `ConsentRepository` with CAS.

You may also inject your own implementation for attribute consent management into CAS that would itself handle storing consent decisions, etc. In order to do this, you will need to design a configuration class that roughly matches the following:

```java
package org.apereo.cas.consent;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    public ConsentRepositoryBuilder consentRepositoryBuilder() {
        ...
    }
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
