### Amazon Configuration

The following options are shared and apply when CAS is configured to integrate with various
Amazon Web Service features, given the provider's *configuration key*:

```properties
# {{ include.configKey }}.credential-access-key=
# {{ include.configKey }}.credential-secret-key=

# {{ include.configKey }}.endpoint=http://localhost:8000
# {{ include.configKey }}.region=US_WEST_2|US_EAST_2|EU_WEST_2|<REGION-NAME>
# {{ include.configKey }}.local-address=
# {{ include.configKey }}.retry-mode=STANDARD|LEGACY

# {{ include.configKey }}.proxy-host=
# {{ include.configKey }}.proxy-password=
# {{ include.configKey }}.proxy-username=

# {{ include.configKey }}.read-capacity=10
# {{ include.configKey }}.write-capacity=10
# {{ include.configKey }}.connection-timeout=5000
# {{ include.configKey }}.socket-timeout=5000
# {{ include.configKey }}.use-reaper=false

# {{ include.configKey }}.client-execution-timeout=10000
# {{ include.configKey }}.max-connections=10
```
