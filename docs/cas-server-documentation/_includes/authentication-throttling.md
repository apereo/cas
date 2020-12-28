CAS provides a facility for limiting failed login attempts to support password guessing and related abuse scenarios.

```properties
# {{ include.configKey }}.username-parameter=username
# {{ include.configKey }}.app-code=CAS

# {{ include.configKey }}.failure.threshold=100
# {{ include.configKey }}.failure.code=AUTHENTICATION_FAILED
# {{ include.configKey }}.failure.range-seconds=60
```
