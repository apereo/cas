---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Configure Service Access Strategy

The access strategy of a registered service provides fine-grained control over the service authorization 
rules. It describes whether the service is allowed to use the CAS server, allowed to participate in
single sign-on authentication, etc. Additionally, it may be configured to require a certain set of 
principal attributes that must exist before access can be granted to the service. This behavior allows 
one to configure various attributes in terms of access roles for the application and define rules that 
would be enacted and validated when an authentication request from the application arrives.

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
