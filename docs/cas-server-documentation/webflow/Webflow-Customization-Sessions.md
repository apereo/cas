---
layout: default
title: CAS - Webflow Session
description: "Where CAS keeps login webflow state: client-side encrypted sessions or server-side sessions in a shared store."
category: Webflow Management
---

{% include variables.html %}

# Webflow Session

CAS uses [Spring Webflow](https://github.com/spring-projects/spring-webflow) to manage the
authentication sequence. Spring Webflow provides a pluggable architecture whereby various actions,
decisions and operations throughout the primary authentication workflow can be easily controlled
and navigated. In order for this navigation to work, some form of conversational session state must be maintained.

{% include_cached actuators.html endpoints="sessions" %}     

## Client-side Sessions

Please see [this guide](Webflow-Customization-Sessions-ClientSide.html).

## Server-side Sessions

Please see [this guide](Webflow-Customization-Sessions-ServerSide.html).
