Control how CAS services definition files should be replicated across a CAS cluster.
Replication modes may be configured per the following options:

| Type                    | Description
|-------------------------|--------------------------------------------------------------
| `ACTIVE_ACTIVE`       | All CAS nodes sync copies of definitions and keep them locally.
| `ACTIVE_PASSIVE`    | Default. One master node keeps definitions and streams changes to other passive nodes.

```properties
# cas.service-registry.stream.enabled=true
# cas.service-registry.stream.replication-mode=ACTIVE_ACTIVE|ACTIVE_PASSIVE
```
