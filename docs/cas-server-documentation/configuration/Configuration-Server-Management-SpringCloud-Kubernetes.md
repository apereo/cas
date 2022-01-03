---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Kubernetes

[Spring Cloud Kubernetes](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/) 
provides implementations of well known Spring Cloud interfaces allowing developers 
to build and run Spring Cloud applications on Kubernetes, and take advantage of configuration management
features in Kubernetes such as `ConfigMap`s and `Secret`s.

Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-kubernetes" %}

{% include_cached casproperties.html 
thirdPartyStartsWith="spring.cloud.kubernetes.secrets,spring.cloud.kubernetes.config" %}
