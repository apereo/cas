---
layout: default
title: CAS - Account Registration Provisioning
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - SCIM Provisioning

{% include_cached casproperties.html properties="cas.account-registration.provisioning.scim" %}

Provisioning tasks can be carried out using the CAS [SCIM integration](../integration/SCIM-Integration.html).
Once enabled and configured, account registration requests may be provisioned via SCIM to other systems.

<div class="alert alert-info"><strong>Usage</strong><p>SCIM integration support for 
delegated authentication is only handled via SCIM <code>v2</code>.</p></div>
