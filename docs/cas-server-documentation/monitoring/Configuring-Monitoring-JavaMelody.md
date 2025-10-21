---
layout: default
title: CAS - JavaMelody Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# JavaMelody Monitoring

Use [JavaMelody](https://github.com/javamelody/javamelody) to monitor CAS in QA and production environments.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
This functionality is not yet compatible with the CAS ecosystem of libraries, and/or Spring Boot. As a result, 
support for this feature is dropped. Please consider using alternatives and keep an eye on future releases of CAS
to see if support for this feature is re-introduced.
</p></div>

Support is added by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-javamelody" %}

JavaMelody monitoring is by default exposed at `${context-path}/monitoring` 
where `${context-path}` is typically set to `/cas`.

## Configuration

{% include_cached casproperties.html thirdPartyStartsWith="javamelody." %}
