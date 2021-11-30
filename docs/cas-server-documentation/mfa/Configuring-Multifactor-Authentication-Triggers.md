---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication Triggers

Triggers can be used to activate and instruct CAS to navigate to a multifactor authentication flow. Each 
trigger should properly try to ignore the authentication request, if applicable configuration 
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

The following triggers are available:

| Trigger                              | Description                                                                                                |
|--------------------------------------|------------------------------------------------------------------------------------------------------------|
| Global                               | [See this page](Configuring-Multifactor-Authentication-Triggers-Global.html).                              |
| Per Application                      | [See this page](Configuring-Multifactor-Authentication-Triggers-PerApplication.html).                      |
| Groovy Per Application               | [See this page](Configuring-Multifactor-Authentication-Triggers-Groovy.html).                              |
| Global Principal Attribute           | [See this page](Configuring-Multifactor-Authentication-Triggers-Global-PrincipalAttribute.html).           |
| Global Principal Attribute Predicate | [See this page](Configuring-Multifactor-Authentication-Triggers-Global-PrincipalAttribute-Predicate.html). |
| Global Authentication Attribute      | [See this page](Configuring-Multifactor-Authentication-Triggers-Global-AuthenticationAttribute.html).      |
| Adaptive                             | [See this page](Configuring-Multifactor-Authentication-Triggers-Adaptive.html).                            |
| Grouper                              | [See this page](Configuring-Multifactor-Authentication-Triggers-Grouper.html).                             |
| Groovy                               | [See this page](Configuring-Multifactor-Authentication-Triggers-Groovy.html).                              |
| REST                                 | [See this page](Configuring-Multifactor-Authentication-Triggers-Rest.html).                                |
| Opt-In Request Parameter/Header      | [See this page](Configuring-Multifactor-Authentication-Triggers-OptInRequest.html).                        |
| Principal Attribute Per Application  | [See this page](Configuring-Multifactor-Authentication-Triggers-PrincipalAttribute-PerApplication.html).   |
| Entity Id Request Parameter          | [See this page](Configuring-Multifactor-Authentication-Triggers-EntityId.html).                            |
| Custom                               | [See this page](Configuring-Multifactor-Authentication-Triggers-Custom.html).                              |
