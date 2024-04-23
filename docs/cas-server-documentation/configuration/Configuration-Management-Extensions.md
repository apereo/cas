---
layout: default
title: CAS - Configuration Extensions
category: Configuration
---

{% include variables.html %}

# Extending CAS Configuration

Being a [Spring Boot](https://github.com/spring-projects/spring-boot) application at its core, designing and extending CAS configuration components 
very much comes down to [the following guide](https://docs.spring.io/spring-boot/docs/current/reference/html/) some aspects 
of which are briefly highlighted in this document.

## Configuration Components

This is the recommended approach to create additional Spring beans, override existing ones and inject your own 
custom behavior into the CAS application runtime.

Given CAS’ adoption of Spring Boot, most if not all of the old XML configuration is transformed into `@AutoConfiguration` 
components. These are classes declared by each relevant module that are automatically picked up at runtime whose job 
is to declare and configure beans and register them into the application context. Another way of thinking about it 
is, components that are decorated with `@AutoConfiguration` are loose equivalents of old XML configuration files that 
are highly organized where `<bean>` tags are translated to java methods tagged with `@Bean` and configured dynamically.

### Design

To design your own configuration class, take inspiration from the following sample:

```java
package org.apereo.cas.custom.config;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SomethingConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public MyBean myBean(
        @Qualifier("someOtherBeanId")
        final SomeBean someOtherBeanId,
        final CasConfigurationProperties casProperties) {
        return new MyBean();
    }
} 
```

- The `@Bean` definitions can also be tagged with `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)` to become auto-reloadable when the CAS 
  context is refreshed as a result of an external property change.
- `@AutoConfiguration` classes can be assigned an order with `@Order(1984)` which would place 
  them in an ordered queue waiting to be loaded in that sequence.

<div class="alert alert-info">:information_source: <strong>To Build & Beyond</strong><p>Note that compiling configuration classes and any other
piece of Java code that is put into the CAS Overlay may require additional CAS modules and dependencies on the classpath. You will need
to study the CAS codebase and find the correct modules that contain the components you need, such 
as <code>CasConfigurationProperties</code> and others.</p></div>

### Register

How are `@AutoConfiguration` components picked up? Each CAS module declares its set of configuration components as such, 
per guidelines [laid out by Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/):

- Create a `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` file
- Add the following into the file:

```
org.apereo.cas.custom.config.SomethingConfiguration
```

### Overrides

What if you needed to override the definition of a CAS-provided bean and replace it entirely with your own?

This is where `@Conditional` components come to aid. Most component/bean definitions in CAS are registered 
with some form of `@Conditional` tag that indicates to the bootstrapping process to ignore their 
creation, if *a bean definition with the same id* is already defined. This means you can create 
your own configuration class, register it and the design a `@Bean` definition only to have the 
context utilize yours rather than what ships with CAS by default.

<div class="alert alert-info">:information_source: <strong>Bean Names</strong><p>To correctly define a conditional <code>Bean</code>, 
you generally need to make sure your own bean definition is created using the same name or identifier as its original equivalent. 
It is impractical and certainly overwhelming to document all runtime bean definitions and their identifiers. So, you will
need to study the CAS codebase to find the correct configuration classes and bean definitions to note their name.</p></div>

### Feature Toggles

[Please see this guide](Configuration-Feature-Toggles.html) to learn more.

## CAS Properties

The collection of CAS-provided settings are all encapsulated inside a `CasConfigurationProperties` component. This 
is a parent class that brings all elements of the entire CAS platform together and binds values to the relevant 
fields inside in a very type-safe manner. The [configuration binding](Configuration-Server-Management.html) is 
typically done via `@EnableConfigurationProperties(CasConfigurationProperties.class)` on the actual configuration class. 

<div class="alert alert-info">:information_source: <strong>Prefix Notation</strong><p>Note that all CAS-provided settings 
exclusively begin with the prefix <code>cas</code>. Other frameworks and packages upon which CAS
depends may present their own configuration naming scheme. Note the difference.</p></div>

If you wish to design your own and extend the CAS configuration file, you can surely follow 
the same approach with the `@EnableConfigurationProperties` annotation or use the good ol' `@Value`.
