---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication Triggers

The following triggers can be used to activate and instruct CAS to navigate to a multifactor authentication flow.

The execution order of multifactor authentication triggers is outlined below:

1. Adaptive
2. Global
3. Opt-In Request Parameter/Header
4. REST Endpoint
5. Groovy Script
6. Principal Attribute Per Application
7. Global Principal Attribute Predicate
8. Global Principal Attribute
9. Global Authentication Attribute
10. Applications
11. Grouper
12. Entity ID Request Parameter
13. Other

Each trigger should properly try to ignore the authentication request, if applicable configuration 
is not found for its activation and execution. Also note that various CAS modules present 
and inject their own *internal triggers* into the CAS application runtime in order to 
translate protocol-specific authentication requests (such as those presented by 
SAML2 or OpenID Connect) into multifactor authentication flows.

<div class="alert alert-info"><strong>Service Requirement</strong><p>Most multifactor authentication 
triggers require that the original authentication request submitted to CAS contain 
a <code>service</code> parameter. Failure to do so will result in an initial successful 
authentication attempt where subsequent requests that carry the relevant parameter 
will elevate the authentication context and trigger multifactor later. If you 
need to test a particular trigger, remember to provide the <code>service</code> 
parameter appropriately to see the trigger in action.</p></div>

The trigger machinery in general should be completely oblivious to multifactor authentication; 
all it cares about is finding the next event in the chain in a very generic way. This means 
that it is technically possible to combine multiple triggers each of which may produce a 
different event in the authentication flow. In the event, having selected a final candidate 
event, the appropriate component and module that is able to support and respond to the 
produced event will take over and route the authentication flow appropriately.

## Global

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Global.html).

## Per Application

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-PerApplication.html).

## Groovy Per Application 

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-PerApplication-Groovy.html).

## Global Principal Attribute

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Global-PrincipalAttribute.html).

## Global Principal Attribute Predicate

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Global-PrincipalAttribute-Predicate.html).

## Global Authentication Attribute

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Global-AuthenticationAttribute.html).

## Adaptive

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Adaptive.html).

## Grouper

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Grouper.html).

## Groovy

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Groovy.html).

## REST

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-Rest.html).

## Opt-In Request Parameter/Header

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-OptInRequest.html).

## Principal Attribute Per Application

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-PrincipalAttribute-PerApplication.html).

## Entity Id Request Parameter

Please see [this guide](Configuring-Multifactor-Authentication-Triggers-EntityId.html).

## Custom

While support for triggers may seem extensive, there is always that edge use case that would 
have you trigger MFA based on a special set of requirements. To learn how to design 
your own triggers, [please see this guide](Configuring-Multifactor-Authentication-Triggers-Custom.html).
