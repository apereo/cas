### Redis Configuration

The following options related to Redis support in CAS apply equally to a number of CAS components (ticket registries, etc):

```properties
# {{ include.configKey }}.redis.host=localhost
# {{ include.configKey }}.redis.database=0
# {{ include.configKey }}.redis.port=6380
# {{ include.configKey }}.redis.password=
# {{ include.configKey }}.redis.timeout=2000
# {{ include.configKey }}.redis.use-ssl=false
# {{ include.configKey }}.redis.read-from=MASTER
```

### Redis Pool Configuration

```properties
# {{ include.configKey }}.redis.pool.enabled=false
# {{ include.configKey }}.redis.pool.max-active=20
# {{ include.configKey }}.redis.pool.max-idle=8
# {{ include.configKey }}.redis.pool.min-idle=0
# {{ include.configKey }}.redis.pool.max-active=8
# {{ include.configKey }}.redis.pool.max-wait=-1
# {{ include.configKey }}.redis.pool.num-tests-per-eviction-run=0
# {{ include.configKey }}.redis.pool.soft-min-evictable-idle-time-millis=0
# {{ include.configKey }}.redis.pool.min-evictable-idle-time-millis=0
# {{ include.configKey }}.redis.pool.lifo=true
# {{ include.configKey }}.redis.pool.fairness=false
# {{ include.configKey }}.redis.pool.test-on-create=false
# {{ include.configKey }}.redis.pool.test-on-borrow=false
# {{ include.configKey }}.redis.pool.test-on-return=false
# {{ include.configKey }}.redis.pool.test-while-idle=false
```

### Redis Sentinel Configuration

```properties
# {{ include.configKey }}.redis.sentinel.master=mymaster
# {{ include.configKey }}.redis.sentinel.node[0]=localhost:26377
# {{ include.configKey }}.redis.sentinel.node[1]=localhost:26378
# {{ include.configKey }}.redis.sentinel.node[2]=localhost:26379
```

### Redis Cluster Configuration

```properties
# {{ include.configKey }}.redis.cluster.password=
# {{ include.configKey }}.redis.cluster.max-redirects=0
# {{ include.configKey }}.redis.cluster.nodes[0].host=
# {{ include.configKey }}.redis.cluster.nodes[0].port=
# {{ include.configKey }}.redis.cluster.nodes[0].replica-of=
# {{ include.configKey }}.redis.cluster.nodes[0].id=
# {{ include.configKey }}.redis.cluster.nodes[0].name=
# {{ include.configKey }}.redis.cluster.nodes[0].type=MASTER|SLAVE
```
