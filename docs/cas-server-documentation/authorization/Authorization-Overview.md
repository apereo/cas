---
layout: default
title: CAS - Authorization
category: Authorization
---

{% include variables.html %}

# Authorization & Access Management

Implementing an authorization strategy to protect applications and relying 
parties in CAS can be done using the following strategies.
 
## Service Access Strategy

Service access strategy allows you to define authorization 
and access strategies that control *entry access* to applications registered with CAS. This strategy typically works best
for web-based applications, regardless of the authentication protocol, and enforces a course-grained access policy
to describe *who* is authorized to access a given application. Once access is granted, the authorization strategy and
by extension CAS itself are completely hands-off and it is then up to the application itself to determine *what* the authenticated
subject is allowed to do or access in the application, typically based on user entitlements, group memberships and other attributes.
     
Read more about this topic [here](../services/Configuring-Service-Access-Strategy.html).

## Fine Grained Authorization

Fine Grained Authorization would allow you to build granular access control using a dedicated language model.
This strategy goes beyond controlling <i>entry access</i> to applications registered with CAS and allows you to define and
develop detailed authorization rules to determine whether a given API request, resource or operation is allowed access. Such API
requests are often proxied by an API gateway which would act as a PEP, routing requests to CAS that is then acting as a PDP.

Read more about this topic [here](Heimdall-Authorization-Overview.html).
