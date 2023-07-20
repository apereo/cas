---
layout: default
title: CAS - Authorization
category: Authorization
---

{% include variables.html %}

# Authorization & Access Management

Implementing an authorization strategy to protect applications and relying 
parties in CAS can be done using the following strategies: 

- [Service Access Strategy](../services/Configuring-Service-Access-Strategy.html): you may define authorization 
and access strategies that control *entry access* to applications registered with CAS. This strategy typically works best
for web-based applications, regardless of the authentication protocol, and enforces a course-grained access policy
to describe *who* is authorized to access a given application. Once access is granted, the authorization strategy and
by extension CAS itself are completely hands-off and it is then up to the application itself to determine *what* the authenticated
subject is allowed to do or access in the application, typically based on user entitlements, group memberships and other attributes.
