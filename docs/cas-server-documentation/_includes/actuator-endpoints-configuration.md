The following properties describe access controls and settings for the `/actuator`
endpoint of CAS which provides administrative functionality and oversight into the CAS software.

```properties
# management.endpoints.enabled-by-default=true
# management.endpoints.web.base-path=/actuator

# management.endpoints.web.exposure.include=info,health,status,configurationMetadata
# management.endpoints.web.exposure.exclude=

# management.endpoints.jmx.exposure.exclude=*
# management.endpoints.jmx.exposure.include=
```
