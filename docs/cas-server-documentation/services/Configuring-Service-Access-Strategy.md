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

<div class="alert alert-info"><strong>Are we sensitive to case?</strong><p>Note that comparison of 
principal/required attribute <strong>names</strong> is
case-sensitive. Exact matches are required for any individual attribute name.</p></div>

<div class="alert alert-info"><strong>Released Attributes</strong><p>Note that if the CAS server is configured to cache 
attributes upon release, all required attributes must also be released to the 
relying party. <a href="../integration/Attribute-Release.html">See this guide</a> for more info on 
attribute release and filters.</p></div>


| Topic                 | Resource                                                |
|-----------------------|---------------------------------------------------------|
| Basic                 | See [this guide](Service-Access-Strategy-Basic.html).   |
| Unauthorized URLs     | See [this guide](Service-Access-Strategy-URL.html).     |
| ABAC                  | See [this guide](Service-Access-Strategy-ABAC.html).    |
| Groovy                | See [this guide](Service-Access-Strategy-Groovy.html).  |
| Time-Based            | See [this guide](Service-Access-Strategy-Time.html).    |
| (Remote) HTTP Request | See [this guide](Service-Access-Strategy-Http.html).    |
| Grouper               | See [this guide](Service-Access-Strategy-Grouper.html). |
| Chaining              | See [this guide](Service-Access-Strategy-Chain.html).   |

