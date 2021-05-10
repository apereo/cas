---
layout: default
title: CAS - RESTful Service Registry
category: Services
---

{% include variables.html %}

# RESTful Service Registry

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-rest-service-registry" %}

## Configuration

{% include casproperties.html properties="cas.service-registry.rest" %}

| Operation         | Method          | Body                       | Response
|-------------------|-----------------|----------------------------------------------------------------------
| Save              | `POST`          | `RegisteredService` object | `RegisteredService` object
| Delete            | `DELETE`        | Service numeric id appended to the endpoint url as a path variable | None
| Load              | `GET`           | None                       | Collection of `RegisteredService` objects
| FindById          | `GET`           | Service numeric id appended to the endpoint url as a path variable   | `RegisteredService` object
| FindById          | `GET`           | Service url appended to the endpoint url as a path variable    | `RegisteredService` object

All operations are expected to return a `200` status code. All other 
response status codes will force CAS to consider the requested operation nullified.

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from 
default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
