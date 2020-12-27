### Cassandra Configuration

Control properties that are relevant to Cassandra,
when CAS attempts to establish connections, run queries, etc.

```properties
# {{ include.configKey }}.keyspace=
# {{ include.configKey }}.contact-points=localhost:9042
# {{ include.configKey }}.local-dc=
# {{ include.configKey }}.consistency-level=ANY|ONE|TWO|THREE|QUORUM|LOCAL_QUORUM|ALL|EACH_QUORUM|LOCAL_SERIAL|SERIAL|LOCAL_ONE
# {{ include.configKey }}.serial-consistency-level=ANY|ONE|TWO|THREE|QUORUM|LOCAL_QUORUM|ALL|EACH_QUORUM|LOCAL_SERIAL|SERIAL|LOCAL_ONE
# {{ include.configKey }}.timeout=PT5S
```
