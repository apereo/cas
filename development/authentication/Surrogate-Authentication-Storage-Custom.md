# Custom Surrogate Authentication

Source: https://apereo.github.io/cas/development/authentication/Surrogate-Authentication-Storage-Custom.html

Decide who may impersonate whom with your own `SurrogateAuthenticationService`, registered with CAS as a Spring bean.

If you wish to design your own account store, you may follow the below approach:

```java
package org.apereo.cas.custom;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MySurrogateConfiguration {

    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
      ...
    }

}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
