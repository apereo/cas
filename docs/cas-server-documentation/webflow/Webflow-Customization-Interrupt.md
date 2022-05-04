---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Authentication Interrupt

CAS has the ability to pause and interrupt the authentication flow to reach 
out to external services and resources, querying for status and settings that 
would then dictate how CAS should manage and control the SSO session. Interrupt 
services are able to present notification messages to the user, provide options 
for redirects to external services, etc. A common use case deals with 
presenting a *bulletin board* during the authentication flow to present 
messages and announcements to select users and then optionally require the 
audience to complete a certain task before CAS is able to honor 
the authentication request and establish a session.

In the interrupt flow, CAS is not at the moment reaching back to an external 
resource acting as an interrupt service to store, track or remember a user's 
decision. In other words, we are only dealing with the `R` (ie. Read) in `CRUD`. 
Today's functionality only deals with inquiring status and reading results 
solely in read-only mode. Interrupt services are themselves required and 
encouraged to redirect the audience to external resources where execution 
of an action resets the interrupt status thereby freeing CAS to proceed 
forward later on without having to interrupt the authentication flow again.  

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-interrupt-webflow" %}

{% include_cached casproperties.html properties="cas.interrupt.core" %}
      
## Tracking Interrupts

The execution of the interrupt inquiry is tracked and remembered as a dedicated CAS cookie and under a specific 
authentication attribute. The calculation of the inquiry trigger would take into account both options, depending on
whether interrupt is set to trigger after authentication or single sign-on.

{% include_cached casproperties.html properties="cas.interrupt.cookie" %}

## Interrupt Payload

Each interrupt strategy is ultimately tasked to produce a response that contains the following settings:

| Field                      | Description                                                                                                                                                           |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `message`                  | Announcement message to display on the screen.                                                                                                                        |
| `links`                    | A map of links to display on the screen where key is the link text and value is the destination.                                                                      |
| `interrupt`                | `true/false` to indicate whether CAS should interrupt the authentication flow.                                                                                        |
| `block`                    | `true/false` to indicate whether CAS should block the authentication flow altogether.                                                                                 |
| `ssoEnabled`               | `true/false` to indicate whether CAS should permit the authentication but not establish SSO.                                                                          |
| `autoRedirect`             | `true/false` to indicate whether CAS should auto-redirect to the first provided link.                                                                                 |
| `autoRedirectAfterSeconds` | Indicate whether CAS should auto-redirect after the configured number of seconds. The default is `-1`, meaning delayed redirect functionality should not be executed. |

## Interrupt Trigger Modes

Authentication interrupts and notifications are executed in the overall flow using one of the following strategies. The
trigger strategy is defined globally via CAS settings.

### After Authentication

This is the default strategy that allows the interrupt query to execute after the
primary authentication event and before the single sign-on event. This means an authenticated user has been 
identified by CAS and by extension is made available to the interrupt, and interrupt has the ability to
decide whether a single sign-on session can be established for the user.

<div class="alert alert-info"><strong>Can We SSO Into Links?</strong><p>
No. The collection of <code>links</code> are just links and are not tied in any way to the 
CAS authentication sequence, meaning they do not activate a state, transition or view in 
that sequence to trigger CAS into generating tickets, executing certain 
actions, etc. Any link in this collection is exactly that; just a link. If a 
link points to applications that are integrated with CAS, accessing those 
applications via the link will prompt the user for credentials again 
specially if single sign-on isn't already established. Remember that 
interrupt notifications typically execute after the authentication step 
and before any single sign-on session is created.</p></div>

### After Single Sign-on

Alternatively, the interrupt query can execute once the single sign-on session has been established.
In this mode, the authenticated user has been identified by CAS and linked to the single sign-on session. Note that
interrupt here loses the ability to decide whether a single sign-on session can be established for the user, and interrupt 
responses indicating this option will have no impact, since the query and interrupt responses 
happen after the creation of the SSO session.

<div class="alert alert-info"><strong>Can We SSO Into Links?</strong><p>
Yes. In this strategy, links to external applications presented by the interrupt response
should be able to take advantage of the established single sign-on session.</p>
</div>

## Interrupt Strategies

Interrupt queries can be executed via the following ways:

| Storage             | Description                                           
|----------------------------------------------------------------------------------
| JSON                | [See this guide](Webflow-Customization-Interrupt-JSON.html).   
| Regex Attribute     | [See this guide](Webflow-Customization-Interrupt-RegexAttribute.html).   
| Groovy              | [See this guide](Webflow-Customization-Interrupt-Groovy.html).   
| REST                | [See this guide](Webflow-Customization-Interrupt-REST.html).   
| Custom              | [See this guide](Webflow-Customization-Interrupt-Custom.html).   

## Interrupt Policy Per Service

Application definitions may be assigned a dedicated webflow interrupt policy. A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "webflowInterruptPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
    "enabled": true,
    "forceExecution": "TRUE"
  }
}
```
  
The following policy settings are supported:

| Field               | Description                                           
|---------------------------------------------------------------------------------------------------------------
| `enabled`           | Whether interrupt notifications are enabled for this application. Default is `true`.
| `forceExecution`    | Whether execution should proceed anyway regardless whether the flow/user is already interrupted. Accepted values are `TRUE`, `FALSE` or `UNDEFINED`.

## Skipping Interrupts

<div class="alert alert-warning"><strong>Usage</strong>
<p><strong>This option is deprecated and is scheduled to be removed in the future</strong>. Consider
assigning a dedicated interrupt policy to the application definition instead.</p>
</div>

Interrupt notifications may be disabled on a per-service basis. A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "skipInterrupt" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "true" ] ]
    }
  }
}
```

{% include_cached registeredserviceproperties.html groups="INTERRUPTS" %}
