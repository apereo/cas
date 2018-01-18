---
layout: default
title: CAS - Services Management Webapp
---

# Services Management Webapp

The management web application is purely an administrative interface that may be deployed in a completely different environment separate from CAS. It allows CAS administrators and application owners delegated access via a graphical user interface so they can manage and modify policies associated with their applications. The operational capacity of the CAS server itself is not in any way tied to the deployment status of the management web application; you may decide to take the application offline for maintenance or completely remove it from your deployment scenario at any given time.

## Overlay Installation

- [Maven Overlay](https://github.com/apereo/cas-management-overlay)
- [Gradle Overlay](https://github.com/apereo/cas-management-gradle-overlay)

## User Attributes

The set of user attributes defined in the CAS Server's [authentication attributes](Configuration-Properties.html#authentication-attributes) or [attribute resolution](Attribute-Resolution.html) configurations should be mapped in the Service Management webapp's configuration using the [stub-based attribute repository](Configuration-Properties.html#attributes). This will make the attributes available for selction in the management webapp's various user attributes-related dropdowns.


The services management webapp is not part of the CAS server and is a standalone web application [located here](https://github.com/apereo/cas-management).
