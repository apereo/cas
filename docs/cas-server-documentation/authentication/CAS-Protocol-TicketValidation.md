---
layout: default
title: CAS - CAS Protocol
category: Protocols
---
{% include variables.html %}

# CAS Protocol - Ticket Validation

Certain aspects of the ticket validation process, as noted by the [CAS protocol specification](../protocol/CAS-Protocol.html), can be customized and augmented using your own dedicated implementations.

<div class="alert alert-warning">:warning: <strong>Caution</strong><p>
Altering the internal mechanics of a CAS server may lead to a problematic insecure configuration and may also jeopradize the population of giant pandas. Such customizations should only be applied if absolutely necessary when all other alternatives are considered and ruled out.
</p></div>

{% tabs ticketvalidation %}

{% tab ticketvalidation Authorization %}
       
You can control whether a ticket validation event can be authorized to allow the operation to proceed.
This may be done by providing a dedicated implementation of `ServiceTicketValidationAuthorizer`
and registering it with the appropriate configuration plan:

```java
@Bean
public ServiceTicketValidationAuthorizerConfigurer ticketValidationAuthorizer() {
    return plan -> plan.registerAuthorizer(...);
}
```

{% endtab %}

{% tab ticketvalidation Matching Strategy %}

Per the CAS Protocol, validating service tickets requires a `service` parameter that is expected to be the identifier of the service for which the service ticket was issued. In other words, CAS requires and enforces an exact match between the given service identifier and one that was supplied originally for ticket creation. To do so, you should start by designing your own configuration component to include the following bean:

```java
@Bean
public ServiceMatchingStrategy serviceMatchingStrategy() {
    return new MyServiceMatchingStrategy(...);
}
```

{% endtab %}

{% endtabs %}

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
