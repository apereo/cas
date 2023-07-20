---
layout: default
title: CAS - Configuring Ticket Expiration Policy Components
category: Ticketing
---

{% include variables.html %}

# Ticket Expiration Policies

CAS supports a pluggable and extensible policy framework to control the expiration policy of ticket-granting
tickets (`TGT`), proxy-granting tickets (`PGT`), service tickets (`ST`), proxy tickets (`PT`), etc.

<div class="alert alert-info">:information_source: <strong>Note</strong><p>There are many other types of 
artifacts in CAS that take the base form of a ticket abstraction. Each protocol or feature may 
introduce a new ticket type that carries its own expiration policy and you will need to 
consult the documentation for that feature or behavior to realize how expiration 
policies for a specific ticket type may be tuned and controlled.</p></div>

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="ticketExpirationPolicies" casModule="cas-server-support-reports" %}

## Available Policies

The following policies are available:

| Storage                   | Description                                                      |
|---------------------------|------------------------------------------------------------------|
| Ticket-granting Tickets   | [See this guide](Configuring-Ticket-Expiration-Policy-TGT.html). |
| Service Tickets           | [See this guide](Configuring-Ticket-Expiration-Policy-ST.html).  |
| Proxy-granting Tickets    | [See this guide](Configuring-Ticket-Expiration-Policy-PGT.html). |
| Proxy Tickets             | [See this guide](Configuring-Ticket-Expiration-Policy-PT.html).  |
| Transient Session Tickets | [See this guide](Configuring-Ticket-Expiration-Policy-TST.html). |
