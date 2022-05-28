---
layout: default
title: CAS - Ticket Registry Locking
category: Ticketing
---

{% include variables.html %}

# Ticket Registry Locking

A number of ticket registries support advanced distributed locking operations for highly concurrent requests. In scenarios
where the CAS server is under very heavy load, it is possible for multiple requests to attempt to alter the server state
at the same time. For example, you might consider service-ticket requests for two separate applications simultaneously
where the resulting tickets and operations would end up changing the state of the single sign-on session, the linked
ticket-granting ticket and how it tracks the two applications. In such scenarios, CAS may be configured to acquire and enforce 
locking operations such that the sequence of executions and requests is handled atomically and proper data and state
is preserved such that concurrent requests would not lead to data loss and missing updates. 

Locking strategies and options are by default available for the default ticket registry implementation. In the event that
the ticket registry technology does support distributed locking, CAS would defer to the distributed lock implementation
that is provided and supported by the ticket registry itself. As always, you may begin to design your own locking strategy
if the registry technology of your choice does not yet support it.

CAS lock implementations are generally backed by [Spring Integration](https://spring.io/projects/spring-integration), and
its behavior can be controlled via CAS settings.

{% include_cached casproperties.html properties="cas.ticket.registry.core" %}
   
## Default

The default lock implementation, generally suitable for single-node deployments, is one 
where the *lock registry* uses *Masked Hashcode* algorithm to obtain and store locks in JVM memory. The default mask is `0xFF` which 
will create an array consisting of `1024 ReentrantLock` instances. When the lock repository attempts to obtain a lock
for a given lock key, (i.e. ticket id), the index of the `Lock` is determined by masking the object's 
hash code and the `Lock` is returned.

## Custom

To design your own locking implementation, you may inject the following `@Bean` into your CAS configuration:

```java
@Bean
public LockRepository casTicketRegistryLockRepository() {
    return new MyLockRepository();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
  
## Others

Distributed locking support is available for some, but not all, ticket registry implementations. Please refer
to the documentation for each ticket registry implementation to learn whether support is available for distributed locking.

