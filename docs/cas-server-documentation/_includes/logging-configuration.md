Control the location and other settings of the CAS logging configuration.

```properties
# logging.config=file:/etc/cas/log4j2.xml
       
# cas.logging.mdc-enabled=true

# Control log levels via properties
# logging.level.org.apereo.cas=DEBUG
```

{% include casproperties.html properties="logging,cas.logging" %}

To disable log sanitization, start the container with the system property `CAS_TICKET_ID_SANITIZE_SKIP=true`.
