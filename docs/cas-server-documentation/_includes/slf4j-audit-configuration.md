Route audit logs to the Slf4j logging system which might in turn store audit logs in a file or any other
destination that the logging system supports.

The logger name is fixed at `org.apereo.inspektr.audit.support`.

```xml
<Logger name="org.apereo.inspektr.audit.support" level="info">
    <!-- Route the audit data to any number of appenders supported by the logging framework. -->
</Logger>
```

<div class="alert alert-info"><strong></strong><p>Audit records routed to the Slf4j log are not
able to read the audit data back given the abstraction layer between CAS, the logging system
and any number of log appenders that might push data to a variety of systems.</p></div>

```properties
# cas.audit.slf4j.audit-format=DEFAULT|JSON
# cas.audit.slf4j.singleline-separator=|
# cas.audit.slf4j.use-single-line=false
# cas.audit.slf4j.enabled=true
```

