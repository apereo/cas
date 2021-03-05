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

<div class="alert alert-info"><strong>Interrupt Sequence</strong><p>
Note that the interrupt operations typically execute after the 
primary authentication event, meaning an authenticated user has 
been identified by CAS and by extension is made available to the interrupt.
</p></div>

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

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-interrupt-webflow" %}

## Interrupt Payload

Each interrupt strategy is ultimately tasked to produce a response that contains the following settings:

| Field                      | Description
|----------------------------|---------------------------------------------------------------------------------
| `message`                  | Announcement message to display on the screen.
| `links`                     | A map of links to display on the screen where key is the link text and value is the destination.
| `interrupt`                | `true/false` to indicate whether CAS should interrupt the authentication flow.
| `block`                    | `true/false` to indicate whether CAS should block the authentication flow altogether.
| `ssoEnabled`               | `true/false` to indicate whether CAS should permit the authentication but not establish SSO.
| `autoRedirect`             | `true/false` to indicate whether CAS should auto-redirect to the first provided link.
| `autoRedirectAfterSeconds` | Indicate whether CAS should auto-redirect after the configured number of seconds. The default is `-1`, meaning delayed redirect functionality should not be executed.

<div class="alert alert-info"><strong>Can We SSO Into Links?</strong><p>
The collection of <code>links</code> are just links and are not tied in any way to the 
CAS authentication sequence, meaning they do not activate a state, transition or view in 
that sequence to trigger CAS into generating tickets, executing certain 
actions, etc. Any link in this collection is exactly that; just a link. If a 
link points to applications that are integrated with CAS, accessing those 
applications via the link will prompt the user for credentials again 
specially if single sign-on isn't already established. Remember that 
interrupt notifications typically execute after the authentication step 
and before any single sign-on session is created.</p></div>

## Interrupt Strategies

Interrupt queries can be executed via the following ways:

### JSON
  
Please [see this guide](Webflow-Customization-Interrupt-JSON.html) for more info.

### Regex Attribute

Please [see this guide](Webflow-Customization-Interrupt-RegexAttribute.html) for more info.

### Groovy

Please [see this guide](Webflow-Customization-Interrupt-Groovy.html) for more info.

### REST

Please [see this guide](Webflow-Customization-Interrupt-REST.html) for more info.

### Custom

Please [see this guide](Webflow-Customization-Interrupt-Custom.html) for more info.

## Skipping Interrupts

Interrupt notifications may be disabled on a per-service basis. A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
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
