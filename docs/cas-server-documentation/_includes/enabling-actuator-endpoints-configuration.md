To determine whether an endpoint is available, the calculation order for all endpoints is as follows:

1. The `enabled` setting of the individual endpoint (i.e. `info`) is consulted in CAS settings, as demonstrated below:

```properties
# management.endpoint.{{ include.configKey }}.enabled=true
```        

2. If undefined, the global endpoint security is consulted from CAS settings.
3. If undefined, the default built-in setting for the endpoint in CAS is consulted, which is typically `false` by default.

A number of available endpoint ids [should be listed here](../monitoring/Monitoring-Statistics.html).

Endpoints may also be mapped to custom arbitrary endpoints. For example, 
to remap the `health` endpoint to `healthcheck`,
specify the following settings:

```properties
# management.endpoints.web.path-mapping.health=healthcheck
```
