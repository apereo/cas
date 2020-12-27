### Couchbase Configuration

The following options are shared and apply when CAS is configured to integrate with Couchbase (i.e ticket registry, etc):

```properties
# {{ include.configKey }}.addresses[0]=localhost
# {{ include.configKey }}.cluster-username=
# {{ include.configKey }}.cluster-password= 

# {{ include.configKey }}.bucket=testbucket    

# {{ include.configKey }}.connection-timeout=PT60S
# {{ include.configKey }}.search-timeout=PT30S
# {{ include.configKey }}.query-timeout=PT30S
# {{ include.configKey }}.view-timeout=PT30S
# {{ include.configKey }}.kv-timeout=PT30S 
# {{ include.configKey }}.max-http-connections=PT30S
# {{ include.configKey }}.idle-connection-timeout=PT30S
# {{ include.configKey }}.query-threshold=PT30S
# {{ include.configKey }}.scan-consistency=NOT_BOUNDED|REQUEST_PLUS
```
