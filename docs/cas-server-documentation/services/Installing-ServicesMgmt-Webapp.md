---
layout: default
title: CAS - Management Webapp
category: Services
---

{% include variables.html %}

# Management Web Application

The management web application is purely an administrative interface that may be deployed in a completely different 
environment separate from CAS. It allows CAS administrators and application owners delegated access via a graphical 
user interface so they can manage and modify policies associated with their applications. The operational capacity 
of the CAS server itself is not in any way tied to the deployment status of the management web application; you may 
decide to take the application offline for maintenance or completely remove it from your deployment scenario at any given time.

## Overlay Installation

The management web application server is not part of the CAS server and 
is a standalone web application which can be deployed using the [CAS Initializr](../installation/WAR-Overlay-Initializr.html).

