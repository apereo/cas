---
layout: default
title: CAS - Configuring Principal Elections
category: Configuration
---
{% include variables.html %}

# Principal Election
 
Each step or branch in the CAS authentication flow may decide to produce an authenticated subject (principal) with attributes
and/or collect attributes and metadata about the authentication event itself. These objects or subjects are eventually
collected and assembled into one canonical authentication object that represents a combination of all attempts, with
all data and attributes merged into a single container. You can of course override the merging strategy by registering
your own `AttributeMerger` component with the runtime context:

```java
@Bean
public AttributeMerger principalElectionAttributeMerger() {
    return new MyAttributeMerger();
}
```
 
You may also override the entire sequence of principal selection and election by registering 
your own `PrincipalElectionStrategy` component with the runtime context:

```java
@Bean
public PrincipalElectionStrategy principalElectionStrategy() {
    return new MyPrincipalElectionStrategy();
}
```

In the event that the chain of CAS authentication attempts produces multiple subjects with variable, conflicting, 
distinct identifiers, there needs to be a strategy defined so CAS may be able to determine which principal 
identifier and object to choose as the basis for the overall authentication attempt at the time of single sign-on.

```java
@Bean
public PrincipalElectionStrategyConflictResolver defaultPrincipalElectionStrategyConflictResolver() {
    return new MyPrincipalElectionStrategyConflictResolver();
}
```

<div class="alert alert-warning">:warning: <strong>Ahoy, Daring Adventurer! </strong><p>Let's put it bluntly: tinkering with 
these components and/or overriding their definition in your CAS build is like trying to teach a squirrel 
algebra – yes, it's possible, but the odds are stacked against you, and it's guaranteed to lead to 
hilariously unfortunate results. Resist the temptation to make changes here.</p></div>

[See this guide](../configuration/Configuration-Management-Extensions.html) to
learn more about how to register configurations into the CAS runtime.
