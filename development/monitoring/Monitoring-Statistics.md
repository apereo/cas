# Monitoring / Statistics

Source: https://apereo.github.io/cas/development/monitoring/Monitoring-Statistics.html

Actuator endpoints used to monitor and diagnose the internal configuration of the CAS server are typically exposed over the endpoint `/actuator`.

> **Definition of Actuator**
>
> This is not a CAS term and the concept comes from Spring Boot. An actuator is a manufacturing term that refers to a mechanical device for moving or controlling something. Actuators can generate a large amount of motion from a small change.

## Actuator Endpoints

The following actuator endpoints are provided:

| Actuator | Reference |
| --- | --- |
| `/actuator/auditevents` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Auditevents.html). |
| `/actuator/beans` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Beans.html). |
| `/actuator/caches` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Caches.html). |
| `/actuator/conditions` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Conditions.html). |
| `/actuator/configprops` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Configprops.html). |
| `/actuator/env` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Env.html). |
| `/actuator/health` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Health.html). |
| `/actuator/heapdump` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Heapdump.html). |
| `/actuator/httpexchanges` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Httpexchanges.html). |
| `/actuator/info` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Info.html). |
| `/actuator/logfile` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Logfile.html). |
| `/actuator/loggers` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Loggers.html). |
| `/actuator/mappings` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Mappings.html). |
| `/actuator/metrics` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Metrics.html). |
| `/actuator/prometheus` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Prometheus.html). |
| `/actuator/quartz` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Quartz.html). |
| `/actuator/sbom` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Sbom.html). |
| `/actuator/scheduledtasks` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Scheduledtasks.html). |
| `/actuator/startup` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Startup.html). |
| `/actuator/threaddump` | [See this guide](https://apereo.github.io/cas/development/monitoring/actuators/Actuator-Endpoint-Threaddump.html). |

> **Usage**
>
> Note that the above table is not a comprehensive list of all possible actuator endpoints in CAS. Each CAS module may present and activate a different set of actuator endpoints specific to a feature such as SSO session management, application registration, and more. Please see the specific documentation for the feature you need to get more details.

## Metrics

Metrics allow to gain insight into the running CAS software, and provide ways to measure the behavior of critical components. See [this guide](https://apereo.github.io/cas/development/monitoring/Configuring-Metrics.html) for more info.
