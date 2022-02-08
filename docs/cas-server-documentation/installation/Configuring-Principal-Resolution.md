---
layout: default
title: CAS - Configuring Principal Resolution
category: Configuration
---
{% include variables.html %}

# Principal Resolution

Principal resolution converts information in the authentication credential into a security principal
that commonly contains additional attributes (i.e. user details such as affiliations, group membership, email, display name). A CAS principal contains a unique identifier by which the authenticated user will be known to all requesting
services. A principal also contains optional [attributes that may be released](../integration/Attribute-Release.html)
to services to support authorization and personalization. Principal resolution is a requisite part of the
authentication process that happens after credential authentication.

The attribute retrieval and resolution process that is carried out 
by principal resolution is [discussed here](../integration/Attribute-Resolution.html).

## Configuration

CAS uses the Person Directory library to provide a flexible principal resolution services against a number of data
sources. The key to configuring `PersonDirectoryPrincipalResolver` is the definition of an `IPersonAttributeDao` object.

{% include_cached casproperties.html properties="cas.person-directory" %}

## Custom
  
You may also design and register your own principal resolution strategy by supplying an implementation of
the `PrincipalResolver` component and registering it with the runtime context:

```java
@Configuration(value = "MyConfiguration", proxyBeanMethods = false)
public static class MyConfiguration {

    @Bean
    public PrincipalResolver myPrincipalResolver() {
        return new MyPrincipalResolver();
    }

    @Bean
    public PrincipalResolutionExecutionPlanConfigurer myExecutionPlanConfigurer(
        @Qualifier("myPrincipalResolver")
        final PrincipalResolver myPrincipalResolver) {
        return plan -> {
            plan.registerPrincipalResolver(myPrincipalResolver);
        };
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to
learn more about how to register configurations into the CAS runtime.
