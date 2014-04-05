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
A cache-backed implementation is recommended for HA deployments, while the default `DefaultTicketRegistry` in-memory component may be suitable for small deployments.


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


### RDBMS Ticket Registries
RDBMS-based ticket registries provide a distributed ticket store across multiple CAS nodes. Components for the following caching technologies are provided:

* [JPA](JPA-Ticket-Registry.html)


### Ticket Generators
CAS presents a pluggable architecture for generating unique ticket ids for each ticket type. The configuration of each generator is defined at `src\main\webapp\WEB-INF\spring-configuration\uniqueIdGenerators.xml`. Here's a brief sample:

{% highlight xml %}

<bean id="ticketGrantingTicketUniqueIdGenerator" class="org.jasig.cas.util.DefaultUniqueTicketIdGenerator"
        c:maxLength="50" c:suffix="${host.name}" />

<bean id="serviceTicketUniqueIdGenerator" class="org.jasig.cas.util.DefaultUniqueTicketIdGenerator"
    c:maxLength="20" c:suffix="${host.name}" />

<bean id="loginTicketUniqueIdGenerator" class="org.jasig.cas.util.DefaultUniqueTicketIdGenerator"
    c:maxLength="30" c:suffix="${host.name}" />
	
<bean id="proxy20TicketUniqueIdGenerator" class="org.jasig.cas.util.DefaultUniqueTicketIdGenerator"
    c:maxLength="20" c:suffix="${host.name}" />
 
<util:map id="uniqueIdGeneratorsMap">
	<entry
		key="org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl"
		value-ref="serviceTicketUniqueIdGenerator" />
</util:map>

{% endhighlight %}

####Components


#####`UniqueTicketIdGenerator`
Strategy parent interface that describes operations needed to generate a unique id for a ticket.


#####`DefaultUniqueTicketIdGenerator`
Uses numeric and random string generators to create a unique id, while supporting prefixes for each ticket type, as is outlined by the CAS protocol, as well as a suffix that typically is mapped to the CAS server node identifier in order to indicate which node is the author of this ticket. The latter configuration point helps with troubleshooting and diagnostics in a clustered CAS environment.


#####`SamlCompliantUniqueTicketIdGenerator`
Unique Ticket Id Generator compliant with the SAML 1.1 specification for artifacts, that is also compliant with the SAML v2 specification.
 

### Ticket Registry Cleaner
The ticket registry cleaner should be used for ticket registries that cannot manage their own state. That would include the default in-memory registry and the JPA ticket registry. Cache-based ticket registry implementation such as Memcached of Ehcache do not require a registry cleaner.


####Components


#####`RegistryCleaner`
Strategy interface to denote the start of cleaning the registry.


#####`DefaultTicketRegistryCleaner`
The default ticket registry cleaner scans the entire CAS ticket registry for expired tickets and removes them.  This process is only required so that the size of the ticket registry will not grow beyond a reasonable size.
The functionality of CAS is not dependent on a ticket being removed as soon as it is expired. Locking strategies may be used to support high availability environments. In a clustered CAS environment with several CAS nodes executing ticket cleanup, it is desirable to execute cleanup from only one CAS node at a time. 


#####`LockingStrategy`
Strategy pattern for defining a locking strategy in support of exclusive execution of some process.


#####`NoOpLockingStrategy`
No-Op locking strategy that allows the use of `DefaultTicketRegistryCleaner` in environments where exclusive access to the registry for cleaning is either unnecessary or not possible.


#####`JpaLockingStrategy`
JPA 2.0 implementation of awn exclusive, non-reentrant lock, to be used with the JPA-backed ticket registry.


####Configuration
If you're using the default ticket registry configuration, your `/cas-server-webapp/WEB-INF/spring-configuration/ticketRegistry.xml` probably looks like this:

{% highlight xml %}
<!-- TICKET REGISTRY CLEANER -->
<bean id="ticketRegistryCleaner" class="org.jasig.cas.ticket.registry.support.DefaultTicketRegistryCleaner"
    p:ticketRegistry-ref="ticketRegistry" />

<bean id="jobDetailTicketRegistryCleaner"  class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean"
    p:targetObject-ref="ticketRegistryCleaner"
    p:targetMethod="clean" />

<bean id="triggerJobDetailTicketRegistryCleaner" class="org.springframework.scheduling.quartz.SimpleTriggerBean"
    p:jobDetail-ref="jobDetailTicketRegistryCleaner"
    p:startDelay="20000"
    p:repeatInterval="5000000" />
{% endhighlight %}

If you're using the JPA ticket registry, your configuration should likely be similar to the following:

{% highlight xml %}
<bean id="ticketRegistryCleaner" class="org.jasig.cas.ticket.registry.support.DefaultTicketRegistryCleaner"
    p:ticketRegistry-ref="ticketRegistry">
   <property name="lock">
      <bean class="org.jasig.cas.ticket.registry.support.JdbcLockingStrategy"
         p:uniqueId="my_unique_machine"
         p:applicationId="cas"
         p:dataSource-ref="dataSource" />
   </property>
</bean>
 
<bean id="jobDetailTicketRegistryCleaner"
       class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean"
        p:targetObject-ref="ticketRegistryCleaner"
        p:targetMethod="clean" />
 
<bean id="triggerJobDetailTicketRegistryCleaner" 
    class="org.springframework.scheduling.quartz.SimpleTriggerBean"
        p:jobDetail-ref="jobDetailTicketRegistryCleaner"
        p:startDelay="20000"
        p:repeatInterval="5000000" />
{% endhighlight %}

This will configure the cleaner with the following defaults:
* tableName = "LOCKS"
* uniqueIdColumnName = "UNIQUE_ID"
* applicationIdColumnName = "APPLICATION_ID"
* expirationDataColumnName = "EXPIRATION_DATE"
* platform = SQL92
* lockTimeout = 3600 (1 hour)



## Ticket Expiration Policies
CAS supports a pluggable and extensible policy framework to control the expiration policy of ticket-granting tickets (TGT) and service tickets (ST). Both TGT and ST expiration policy beans are defined in the `/cas-server-webapp/src/main/webapp/WEB-INF/spring-configuration/ticketExpirationPolicies.xml` file in the CAS distribution. 

<div class="alert alert-info"><strong>Policies Are Not Ticket-Specific</strong><p>Ticket expiration policies are not specific to a particular kind of ticket, so it is possible to apply a policy intended for service tickets to ticket-granting tickets, although it may make little sense to do so.</p></div>


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

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Use of this policy has significant consequences to overall security policy and should be enabled only after thorough review by a qualified security team. There are also implications to server resource usage for the ticket registries backed by filesystem storage. Since disk storage for tickets can never be reclaimed for those registries with this policy in effect, use of this policy with those ticket registry implementations is strongly discouraged.</p></div>


#####Usage Example
{% highlight xml %}
<!-- TGT never expires -->
<bean id="grantingTicketExpirationPolicy" class="org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy" />
{% endhighlight %}


####`RememberMeDelegatingExpirationPolicy`
This policy implements applies to [long term authentication](Configuring-Authentication-Components.html) features of CAS known as "Remember Me". 


#####Usage Example
{% highlight xml %}
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
{% highlight xml %}
<!-- ST may be used exactly once and must be validated within 10 seconds. -->
<util:constant id="SECONDS" static-field="java.util.concurrent.TimeUnit.SECONDS"/>
<bean id="serviceTicketExpirationPolicy" class="org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy"
      c:numberOfUses="1" c:timeToKillInMilliSeconds="10" c:timeUnit-ref="SECONDS" />
{% endhighlight %}


