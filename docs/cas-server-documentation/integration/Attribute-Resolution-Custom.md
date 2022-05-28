---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Custom Attribute Resolution

You may also design and inject your own attribute repository and principal resolution implementation 
into CAS that would itself handle fetching attributes and resolving persons, etc. In order
to do this, you will need to design a configuration class that roughly matches the following:

```java
package org.apereo.cas.custom;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    public IPersonAttributeDao myPersonAttributeDao() {
        return new MyPersonAttributeDao(...);
    }

    @Bean
    public PersonDirectoryAttributeRepositoryPlanConfigurer myAttributeRepositoryPlanConfigurer(
        @Qualifier("myPersonAttributeDao")
        final IPersonAttributeDao myPersonAttributeDao) {
        return plan -> plan.registerAttributeRepository(myPersonAttributeDao);
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to
learn more about how to register configurations into the CAS runtime.
