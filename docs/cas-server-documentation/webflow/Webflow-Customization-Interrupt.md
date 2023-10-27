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

## Interrupt Payload
         
The interrupt response payload that is ultimately passed down to the CAS user interface is produced by
interrupt strategies described below. The payload structure regardless of the producer strategy is shown here
by the [JSON interrupt strategy](Webflow-Customization-Interrupt-JSON.html).

## Interrupt Trigger Modes
        
Please [see this guide](Webflow-Customization-Interrupt-TriggerModes.html) to learn more.

## Interrupt Strategies

Interrupt queries can be executed via the following ways:

| Storage         | Description                                                            |
|-----------------|------------------------------------------------------------------------|
| JSON            | [See this guide](Webflow-Customization-Interrupt-JSON.html).           |
| Regex Attribute | [See this guide](Webflow-Customization-Interrupt-RegexAttribute.html). |
| Groovy          | [See this guide](Webflow-Customization-Interrupt-Groovy.html).         |
| REST            | [See this guide](Webflow-Customization-Interrupt-REST.html).           |
| Custom          | [See this guide](Webflow-Customization-Interrupt-Custom.html).         |
