---
layout: default
title: CAS - Configure Service Access Strategy
description: "Access strategies for registered applications: enable or disable access, require attributes, restrict by time or delegate the decision to an external engine."
category: Services
---

{% include variables.html %}

# Configure Service Access Strategy

The access strategy of a registered service provides fine-grained control over the service authorization 
rules. It describes whether the service is allowed to use the CAS server, allowed to participate in
single sign-on authentication, etc. Additionally, it may be configured to require a certain set of 
principal attributes that must exist before access can be granted to the service. This behavior allows 
one to configure various attributes in terms of access roles for the application and define rules that 
would be enacted and validated when an authentication request from the application arrives. When the request belongs to a
single sign-on session, the attributes the access strategy evaluates also include those of the authentication, such as
`clientIpAddress`, the address of the client recorded when the user signed in.

| Strategy                 | Resource                                                                |
|--------------------------|-------------------------------------------------------------------------|
| Basic                    | See [this guide](Service-Access-Strategy-Basic.html).                   |
| Unauthorized URLs        | See [this guide](Service-Access-Strategy-URL.html).                     |
| ABAC                     | See [this guide](Service-Access-Strategy-ABAC.html).                    |
| Groovy                   | See [this guide](Service-Access-Strategy-Groovy.html).                  |
| Time-Based               | See [this guide](Service-Access-Strategy-Time.html).                    |
| (Remote) HTTP Request    | See [this guide](Service-Access-Strategy-Http.html).                    |
| Grouper                  | See [this guide](Service-Access-Strategy-Grouper.html).                 |
| AWS Verified Permissions | See [this guide](Service-Access-Strategy-AWS-VerifiedPermissions.html). |
| OpenFGA                  | See [this guide](Service-Access-Strategy-OpenFGA.html).                 |
| Permify                  | See [this guide](Service-Access-Strategy-Permify.html).                 |
| Cerbos                   | See [this guide](Service-Access-Strategy-Cerbos.html).                  |
| Open Policy Agent        | See [this guide](Service-Access-Strategy-OpenPolicyAgent.html).         |
| Chaining                 | See [this guide](Service-Access-Strategy-Chain.html).                   |
| Custom                   | See [this guide](Service-Access-Strategy-Custom.html).                  |
| SCIM                     | See [this guide](Service-Access-Strategy-SCIM.html).                    |

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="serviceAccess" casModule="cas-server-support-reports" %}

The `serviceAccess` endpoint explains its answer, whether access is allowed or refused, as the steps CAS goes
through: whether an application matches and is enabled, active and not expired, whether the user signed in, and the
access strategy's activation and required and rejected attributes, one attribute at a time with the user's values.
A strategy that decides by other means, such as an external authorization service or a script, including attribute
rules that use scripts, is asked once, when CAS decides, and reported with that answer; within a chain, such a strategy
is not asked again to explain its part, and a chain of strategies is reported with the steps of each. An application
is named by its service URL, by the client id of an OAuth or OpenID Connect application, or by the entity id of a SAML2
service provider; a client id or an entity id alone is only answered by such an application. The answer names the
application without its definition, and a check that fails for another reason answers with a server error rather than
a decision.
