{% include {{ version }}/ldap-configuration.md configKey="cas.monitor.ldap[0]" %}

The default for the pool size is zero to prevent failed ldap pool initialization to impact server startup.

```properties
# cas.monitor.ldap[0].max-wait=5000
# cas.monitor.ldap[0].pool.min-size=0
# cas.monitor.ldap[0].pool.max-size=18
# cas.monitor.ldap[0].pool.enabled=true
```
