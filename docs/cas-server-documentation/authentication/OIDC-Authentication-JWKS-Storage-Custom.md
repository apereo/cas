---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication JWKS Storage - Custom

It is possible to design and inject your own keystore generation strategy into CAS using the following `@Bean`
that would be registered in a `@AutoConfiguration` class:

```java
@Bean(initMethod = "generate")
public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService() {
    return new MyJsonWebKeystoreGeneratorService(...);
}
```

Your configuration class needs to be registered 
with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.
