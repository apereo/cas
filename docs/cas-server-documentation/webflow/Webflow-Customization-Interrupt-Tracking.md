---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Tracking Authentication Interrupts

The execution of the interrupt inquiry is tracked and remembered as a dedicated CAS cookie and under a specific
authentication attribute. The calculation of the inquiry trigger would take into account both options, depending on
whether interrupt is set to trigger after authentication or single sign-on.

The default tracking mechanism is also able to detect and pick up changes in previous interrupt responses
and re-interrupt the user if necessary. For example, if the user has been interrupted once and the underlying data
and interrupt payload has changed since that last notification, CAS may interrupt the user flow again to process
the most recent version of the interrupt payload.

{% include_cached casproperties.html properties="cas.interrupt.cookie" %}

<div class="alert alert-warning">:warning: <strong>Pay Attention</strong><br /> 
The interrupt tracking mechanism relies on a cookie by default to remember the previous interrupt payload for future comparisons.
Be careful with the size of the interrupt payload you produce, as it may affect the
overall size of the cookie accepted by the browser or the server container of choice.
</div>

## Custom
 
If you wish to design your own interrupt tracking mechanism, you
may plug in a custom implementation of the `InterruptTrackingEngine` that
allows you to handle this on your own:
                
```java
@Bean
public InterruptTrackingEngine interruptTrackingEngine() {
    return new MyInterruptTrackingEngine();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
