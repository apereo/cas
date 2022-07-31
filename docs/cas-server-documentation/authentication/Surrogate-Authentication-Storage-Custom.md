---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# Custom Surrogate Authentication

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

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
