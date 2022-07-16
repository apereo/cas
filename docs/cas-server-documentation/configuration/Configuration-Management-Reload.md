---
layout: default
title: CAS - Configuration Management - Reloading Changes
category: Configuration
---

{% include variables.html %}

# Reloading Changes

The CAS spring cloud configuration server is able to consume properties and settings
via the [profiles outlined here](Configuration-Server-Management.html). The server is constantly monitoring
changes to the underlying property sources automatically, but has no way to broadcast those changes
to its own clients, such as the CAS server itself, which would act as *a client of the configuration
server* expecting change notifications to quietly reload its configuration.

Therefore, in order to broadcast such `change` events CAS
presents [various endpoints](../monitoring/Monitoring-Statistics.html) that allow the adopter
to **refresh** the configuration as needed. This means that an adopter would 
change a required CAS settings and then would submit
a request to CAS to refresh its current state. All CAS internal components that are affected
by the external change are quietly reloaded
and the setting takes immediate effect, completely removing the need for container restarts or CAS re-deployments.

<div class="alert alert-info"><strong>Do Not Discriminate!</strong><p>Most if not all CAS settings are eligible candidates
for reloads. CAS should be smart enough to reload the appropriate configuration, regardless of setting/module that
ends up using that setting. All is fair game, as the entire CAS web application inclusive of all modules and all
relevant settings may be completely and utterly reloadable. If you find an instance where this statement does not hold, please speak up.</p></div>

## Reload Strategy

CAS uses [Spring Cloud](https://github.com/spring-cloud/spring-cloud-config) to manage the internal state of the configuration. The configuration server that
is provided by Spring Cloud embedded in CAS is constantly monitoring sources that house CAS settings and upon changes will auto-refresh itself.

The CAS application context and runtime environment that contains all Spring components and bean definitions
can be reloaded using the following administrative endpoints:

{% include_cached actuators.html endpoints="features,refresh,busenv,bus-refresh,busrefresh,serviceregistry" %}

In the event that the [standalone configuration profile](Configuration-Server-Management.html#configuration-strategies) is used to control and direct settings and Spring Cloud configuration server is disabled, CAS may begin to automatically watch and monitor the configuration files indicated by the profile and will auto-reload the state of the runtime application context automatically.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-core-events-configuration" %}

Spring application context will fail to refresh beans that are excluded (or conditionally activated/created) at initialization/startup time, because there is nothing to refresh to begin with. Refresh requests and beans marked with `@RefreshScope` only work in scenarios where there is an existing reference to a bean in the application context hierarchy that can be refreshed; beans or configuration classes that are skipped during the startup and application context initialization will never be refreshable, because they are not re-created upon refresh requests. In other words, refresh requests only work best when there is a setting or property whose existing value changes from A to B; if there was no A to begin with, or if A is being removed, refresh requests and the reload strategy may fall short.
