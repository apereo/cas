---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Attribute Repository Selection
     
Principal attributes that are retrieved from dedicated [attribute repositories](Attribute-Resolution.html) 
through the process of [principal resolution](../installation/Configuring-Principal-Resolution.html) are activated
and selected via the following strategies:

- Attribute repositories that are defined in the CAS configuration and remain in an active state are selected and invoked automatically by the attribute resolution engine.
- Attribute repositories can also be optionally [assigned to service definitions](Attribute-Release-RepositoryFiltering.html) and are then only activated and invoked when the application registration policy is activated for the request.

## Custom

If you wish to design your own attribute repository selection mechanism, you may follow the below approach:

```java
@Bean
public AttributeRepositoryResolver attributeRepositoryResolver() {
    return new MyAttributeRepositoryResolver();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
