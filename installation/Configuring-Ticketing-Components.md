---
layout: default
title: CAS - Configuring Ticketing Components
---
# Configuring Ticketing Components
There are two core configurable ticketing components:

* `TicketRegistry` - Provides for durable ticket storage.
* `ExpirationPolicy` - Provides a policy framework for ticket expiration semantics.

## Ticket Registry
The deployment environment and technology expertise generally determine the particular `TicketRegistry` component.
A cache-backed implementation is recommended for HA deployments, while the default `DefaultTicketRegistry` in-memory
component may be suitable for small deployments.

### Default (In-Memory) Ticket Registry
`DefaultTicketRegistry` uses a `ConcurrentHashMap` for memory-backed ticket storage and retrieval.
This component does not preserve ticket state across restarts. There are a few configuration knobs available:

* `initialCapacity` - `ConcurrentHashMap` initial capacity.
* `loadFactor` - `ConcurrentHashMap` load factor.
for more information.
* `concurrencyLevel` - Allows tuning the `ConcurrentHashMap` for concurrent write support.

All three arguments map to those of the [`ConcurrentHashMap` constructor](http://goo.gl/qKKg7).
{% highlight xml %}
<bean id="ticketRegistry"
      class="org.jasig.cas.ticket.registry.DefaultTicketRegistry"
      c:initialCapacity="10000"
      c:loadFactor="1"
      c:concurrencyLevel="20" />
{% endhighlight %}

### Cache-Based Ticket Registries
Cached-based ticket registries provide a high-performance solution for ticket storage in high availability
deployments. Components for the following caching technologies are provided:

* [Ehcache](Ehcache-Ticket-Registry.html)
* [JBoss Cache](JBoss-Cache-Ticket-Registry.html)
* [Memcached](Memcached-Ticket-Registry.html)

## Ticket Expiration Policies
CAS supports a pluggable and extensible policy framework to control the expiration policy of ticket-granting tickets (TGT) and service tickets (ST). Both TGT and ST expiration policy beans are defined in the `/cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/ticketExpirationPolicies.xml` file in the CAS distribution. 

Ticket expiration policies are not specific to a particular kind of ticket, so it is possible to apply a policy intended for service tickets to ticket-granting tickets, although it may make little sense to do so.

### Ticket-Granting Ticket Policies
TGT expiration policy governs the time span during which an authenticated user may grant STs with a valid (non-expired) TGT without having to reauthenticate. An attempt to grant a ST with an expired TGT would require the user to reauthenticate to obtain a new (valid) TGT.

####`TimeoutExpirationPolicy`
The default expiration policy applied to TGTs provides for most-recently-used expiration policy, similar to a Web server session timeout. For example, a 2-hour time span with this policy in effect would require a TGT to be used every 2 hours or less, otherwise it would be marked as expired.

##### Parameters

* `timeToKillInMilliSeconds` - Maximum amount of inactivity in ms from the last time the ticket was used beyond which it is considered expired.

##### Usage Example
{% highlight xml %}
<!-- TGT expires after 2 hours in inactivity -->
<bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.TimeoutExpirationPolicy"
      c:timeToKillInMilliSeconds="7200000" />
{% endhighlight %}

####`HardTimeoutExpirationPolicy`
The hard timeout policy provides for finite ticket lifetime as measured from the time of creation. For example, a 4-hour time span for this policy means that a ticket created at 1PM may be used up until 5PM; subsequent attempts to use it will mark it expired and the user will be forced to reauthenticate.

##### Parameters
* `timeToKillInMilliSeconds` - Total ticket lifetime in ms.

##### Usage Example
{% highlight xml %}
<!-- TGT expires 4 hours after creation -->
<bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.HardTimeoutExpirationPolicy">
  c:timeToKillInMilliSeconds="14400000" />
{% endhighlight %}


####`ThrottledUseAndTimeoutExpirationPolicy`
The throttled timeout policy extends the TimeoutExpirationPolicy with the concept of throttling where a ticket may be used at most every N seconds. This policy was designed to thwart denial of service conditions where a rogue or misconfigured client attempts to consume CAS server resources by requesting high volumes of service tickets in a short time.

##### Parameters
* `timeToKillInMilliSeconds` - Maximum amount of inactivity in ms from the last time the ticket was used beyond which it is considered expired.
* `timeInBetweenUsesInMilliSeconds` - The minimum amount of time permitted between consecutive uses of a ticket.

##### Usage Example
{% highlight xml %}
<!--
TGT expires under one of two conditions:
 * More than 3 hours of inactivity
 * Used consecutively where less than 5 seconds has elapsed from the first use
-->
<bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy"
  p:timeToKillInMilliSeconds="10800000"
  p:timeInBetweenUsesInMilliSeconds="5000"
/>
{% endhighlight %}

####`NeverExpiresExpirationPolicy`
The never expires policy allows tickets to exist indefinitely.

Use of this policy has significant consequences to overall security policy and should be enabled only after thorough review by a qualified security team. There are also implications to server resource usage for the ticket registries backed by filesystem storage, e.g. JpaTicketRegistry and BerkleyDB. Since disk storage for tickets can never be reclaimed for those registries with this policy in effect, use of this policy with those ticket registry implementations is strongly discouraged.

#####Usage Example
{% highlight %}
<!-- TGT never expires -->
<bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy" />
{% endhighlight %}

####`RememberMeDelegatingExpirationPolicy`
This policy implements the [Remember Me feature](RememberMe-Authentication.html) for long-term ticket-granting tickets. 

#####Usage Example
{% highlight %}
<bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.RememberMeDelegatingExpirationPolicy">
   <property name="sessionExpirationPolicy">
        <bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.TimeoutExpirationPolicy"
                c:timeToKillInMilliSeconds="xxxxxx" />
   </property>
   <property name="rememberMeExpirationPolicy">
        <bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.TimeoutExpirationPolicy"
                c:timeToKillInMilliSeconds="xxxxxx" />
   </property>
</bean>
{% endhighlight %}

### Service Ticket Policies

####`MultiTimeUseOrTimeoutExpirationPolicy`
This is the default policy applied to service tickets where a ticket is expired after a fixed number of uses or after a maximum period of inactivity elapses.

#####Parameters
* `numberOfUses` - Maximum number of times the ticket can be used.
* `timeToKill` - Maximum amount of inactivity from the last time the ticket was used beyond which it is considered expired.
* `timeUnit` - The unit of time based on which `timeToKill` will be calculated.

#####Usage Example
{% highlight %}
<!-- ST may be used exactly once and must be validated within 10 seconds. -->
<util:constant id="SECONDS" static-field="java.util.concurrent.TimeUnit.SECONDS"/>
<bean id="serviceTicketExpirationPolicy" class="org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy"
      c:numberOfUses="1" c:timeToKillInMilliSeconds="10" c:timeUnit-ref="SECONDS" />
{% endhighlight %}


