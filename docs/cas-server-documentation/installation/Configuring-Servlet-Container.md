---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Servlet Container Configuration

A number of container options are available to deploy CAS. The [WAR Overlay](WAR-Overlay-Installation.html) guide
describes how to build and deploy CAS.

## How Do I Choose?

There are is a wide range of servlet containers and servers on the menu. The selection criteria is outlined below:

- Choose a technology that you are most familiar with and have the skills and patience to troubleshoot, tune and scale for the win. 
- Choose a technology that does not force your CAS configuration to be tied to any individual 
  servers/nodes in the cluster, as this will present auto-scaling issues and manual effort.
- Choose a technology that works well with your network and firewall configuration and is performant and reliable enough based on your network topology.
- Choose a technology that shows promising results under *your expected load*, having run [performance and stress tests](../high_availability/High-Availability-Performance-Testing.html).
- Choose a technology that does not depend on outside processes, systems and manual work as much as possible, is self-reliant and self contained.

## Production Quality

All servlet containers presented here, embedded or otherwise, aim to be production ready. This means 
that CAS ships with useful defaults out of the box that 
may be overridden, if necessary and by default, CAS configures everything for you from development to production 
in today’s platforms. In terms of their production quality, there is almost no 
difference between using an embedded container vs. an external one.

Unless there are specific, technical and reasonable objections, choosing an 
embedded servlet container is almost always the better choice.

## Actuator Endpoints

The following endpoints are provided:

{% include actuators.html endpoints="shutdown,restart,pause,resume" %}

## Embedded
     
Please see [this guide](Configuring-Servlet-Container-Embedded.html).

## External

Please see [this guide](Configuring-Servlet-Container-External.html)

## Docker

You may also be interested to deploy CAS via [Docker](https://www.docker.com/).
See [this guide](Docker-Installation.html) for more info.

## System Service

CAS can be easily started as Unix/Linux services using either `init.d` or `systemd`. To learn 
more, please [visit this guide](Configuring-Deployment-System-Service.html).
