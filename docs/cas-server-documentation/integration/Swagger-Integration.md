---
layout: default
title: CAS - Swagger API Integration
category: Integration
---

{% include variables.html %}

# Overview

CAS takes advantages of [Swagger](https://swagger.io/) to produce API documentation automatically. 
The generated documentation supports all CAS endpoints and REST APIs provided they are made available to the runtime 
application context and are present in the overlay. 

Support is enabled by including the following dependency in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-swagger" %}

{% include casproperties.html thirdPartyStartsWith="springdoc." %}

## Endpoints

The following Swagger endpoints may be used to analyze and test the APIs: 

| Description                 | URL              
|-----------------------------|-------------------------------------------------
| Swagger API Specification   | `https://sso.example.org/cas/v3/api-docs`       
| Swagger UI                  | `https://sso.example.org/cas/swagger-ui.html`       
