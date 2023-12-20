---
layout: default
title: CAS - Webflow Decorations
category: Webflow Management
---

{% include variables.html %}

# REST Decorators - Webflow Decorations

RESTful login decorators allow one to inject data into the webflow context by 
reaching out to an external REST API. If the endpoint responds back with a `200` 
status code, CAS would parse the response body into a JSON object and will stuff 
the result into the webflow's `flowScope` container under the key `decoration`. 
Please remember that data stuffed into the webflow **MUST** be serializable and 
if you intend to pass along complex objects types and fancy data structures, you 
need to make sure they can safely and ultimately transform into a simple `byte[]`.

{% include_cached casproperties.html properties="cas.webflow.login-decorator.rest" %}

