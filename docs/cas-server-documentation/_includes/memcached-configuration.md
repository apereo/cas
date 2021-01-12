### Memcached Configuration

The following  options are shared and apply when CAS is configured to integrate with memcached:

```properties
# {{ include.configKey }}.memcached.servers=localhost:11211
# {{ include.configKey }}.memcached.locator-type=ARRAY_MOD
# {{ include.configKey }}.memcached.failure-mode=Redistribute
# {{ include.configKey }}.memcached.hash-algorithm=FNV1_64_HASH
# {{ include.configKey }}.memcached.should-optimize=false
# {{ include.configKey }}.memcached.daemon=true
# {{ include.configKey }}.memcached.max-reconnect-delay=-1
# {{ include.configKey }}.memcached.use-nagle-algorithm=false
# {{ include.configKey }}.memcached.shutdown-timeout-seconds=-1
# {{ include.configKey }}.memcached.op-timeout=-1
# {{ include.configKey }}.memcached.timeout-exception-threshold=2
# {{ include.configKey }}.memcached.max-total=20
# {{ include.configKey }}.memcached.max-idle=8
# {{ include.configKey }}.memcached.min-idle=0

# {{ include.configKey }}.memcached.transcoder=KRYO|SERIAL|WHALIN|WHALINV1
# {{ include.configKey }}.memcached.transcoder-compression-threshold=16384
# {{ include.configKey }}.memcached.kryo-auto-reset=false
# {{ include.configKey }}.memcached.kryo-objects-by-reference=false
# {{ include.configKey }}.memcached.kryo-registration-required=false
```
