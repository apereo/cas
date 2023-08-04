---
layout: default
title: CAS - Admin Console
category: Installation
---

{% include variables.html %}
 
# Admin Console & Dashboard
   
CAS provides a number of facilities and dashboard that can be used to administer and manage the CAS server deployment.
Such options usually are not mutually exclusive and are designed to work together and present various aspects
of the CAS configuration and build that might include application registrations, configuration properties, etc.

{% tabs casadminoptions %}

{% tab casadminoptions Palantir %}

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-palantir" %}

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
This capability is a work in progress. We encourage you to start to experiment and test your CAS deployment 
with this feature and contribute fixes.</p></div>

{% endtab %}

{% tab casadminoptions Spring Boot Admin %}

CAS takes advantage of Spring Boot Admin server to manage and monitor its internal state visually. As a Spring Boot Admin client, CAS registers 
itself with the Spring Boot Admin server over HTTP and reports back its status and health to the server’s web interface.
     
More details about this integration is [available here](../monitoring/Configuring-SpringBootAdmin.html).

{% endtab %}

{% endtabs %}
