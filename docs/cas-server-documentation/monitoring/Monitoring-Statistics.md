---
layout: default
title: CAS - Monitoring & Statistics
category: Monitoring & Statistics
---

{% include variables.html %}

# Monitoring / Statistics

Actuator endpoints used to monitor and diagnose the internal 
configuration of the CAS server are typically exposed over the endpoint `/actuator`.

<div class="alert alert-info">:information_source: <strong>Definition of Actuator</strong><p>
This is not a CAS term and the concept comes from Spring Boot. An actuator is a manufacturing term that refers to a mechanical device 
for moving or controlling something. Actuators can generate a large amount of motion from a small change.
</p></div>

## Actuator Endpoints

{% assign actuators = "logfile,auditevents,beans,caches,conditions,configprops,env,httpexchanges,loggers,info,startup,threaddump,health,metrics,httptrace,mappings,scheduledtasks,heapdump,prometheus,startup,quartz,sbom" | split: "," | sort %}

The following actuator endpoints are provided:

<table class="cas-datatable actuators-table" data-page-length="15">
  <thead>
    <tr><th>Actuator</th><th>Reference</th></tr>
  </thead>
  <tbody>
    {% for endpoint in actuators %}
    <tr>
    <td><code>/actuator/{{ endpoint | downcase }}</code></td>
    <td><a href="actuators/Actuator-Endpoint-{{ endpoint | capitalize }}.html">See this guide</a>.</td>
    </tr>
    {% endfor %}
  </tbody>
</table>

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Note that the above table is not a comprehensive list of all possible actuator endpoints in CAS. Each CAS module may present
and activate a different set of actuator endpoints specific to a feature such as SSO session management, 
application registration, and more. Please see the specific documentation for the feature you need to get more details.</p></div>

## Metrics

Metrics allow to gain insight into the running CAS software, and provide 
ways to measure the behavior of critical components. 
See [this guide](Configuring-Metrics.html) for more info.
