#### Hazelcast Configuration

The following options related to Hazelcast support in CAS apply equally to a number of CAS components:

```properties
# {{ include.configKey }}.cluster.members=123.456.789.000,123.456.789.001
# {{ include.configKey }}.cluster.instance-name=localhost
# {{ include.configKey }}.cluster.port=5701

# {{ include.configKey }}.license-key=
# {{ include.configKey }}.enable-compression=false
# {{ include.configKey }}.enable-management-center-scripting=true
```

More advanced Hazelcast configuration settings are listed below, given the component's *configuration key*:

```properties
# {{ include.configKey }}.cluster.tcpip-enabled=true

# {{ include.configKey }}.cluster.partition-member-group-type=HOST_AWARE|CUSTOM|PER_MEMBER|ZONE_AWARE|SPI
# {{ include.configKey }}.cluster.map-merge-policy=PUT_IF_ABSENT|HIGHER_HITS|DISCARD|PASS_THROUGH|EXPIRATION_TIME|LATEST_UPDATE|LATEST_ACCESS

# {{ include.configKey }}.cluster.eviction-policy=LRU
# {{ include.configKey }}.cluster.max-no-heartbeat-seconds=300
# {{ include.configKey }}.cluster.logging-type=slf4j
# {{ include.configKey }}.cluster.port-auto-increment=true
# {{ include.configKey }}.cluster.max-size=85
# {{ include.configKey }}.cluster.backup-count=1
# {{ include.configKey }}.cluster.async-backup-count=0
# {{ include.configKey }}.cluster.max-size-policy=USED_HEAP_PERCENTAGE
# {{ include.configKey }}.cluster.timeout=5

# {{ include.configKey }}.cluster.local-address=
# {{ include.configKey }}.cluster.public-address=
# {{ include.configKey }}.cluster.network-interfaces=1,2,3,4

# {{ include.configKey }}.cluster.outbound-ports[0]=45000
```

#### Static WAN Replication

```properties
# {{ include.configKey }}.cluster.wan-replication.enabled=false
# {{ include.configKey }}.cluster.wan-replication.replication-name=CAS

# {{ include.configKey }}.cluster.wan-replication.targets[0].endpoints=1.2.3.4,4.5.6.7
# {{ include.configKey }}.cluster.wan-replication.targets[0].publisher-className=com.hazelcast.enterprise.wan.replication.WanBatchReplication
# {{ include.configKey }}.cluster.wan-replication.targets[0].queue-full-behavior=THROW_EXCEPTION
# {{ include.configKey }}.cluster.wan-replication.targets[0].acknowledge-type=ACK_ON_OPERATION_COMPLETE
# {{ include.configKey }}.cluster.wan-replication.targets[0].queue-capacity=10000
# {{ include.configKey }}.cluster.wan-replication.targets[0].batch-size=500
# {{ include.configKey }}.cluster.wan-replication.targets[0].snapshot-enabled=false
# {{ include.configKey }}.cluster.wan-replication.targets[0].batch-maximum-delay-milliseconds=1000
# {{ include.configKey }}.cluster.wan-replication.targets[0].response-timeout-milliseconds=60000
# {{ include.configKey }}.cluster.wan-replication.targets[0].executor-thread-count=2

# {{ include.configKey }}.cluster.wan-replication.targets[0].consistency-check-strategy=NONE|MERKLE_TREES
# {{ include.configKey }}.cluster.wan-replication.targets[0].cluster-name=
# {{ include.configKey }}.cluster.wan-replication.targets[0].publisher-id=
# {{ include.configKey }}.cluster.wan-replication.targets[0].properties=
```

#### Multicast Discovery

```properties
# {{ include.configKey }}.cluster.multicast-trusted-interfaces=
# {{ include.configKey }}.cluster.multicast-enabled=false
# {{ include.configKey }}.cluster.multicast-port=
# {{ include.configKey }}.cluster.multicast-group=
# {{ include.configKey }}.cluster.multicast-timeout=2
# {{ include.configKey }}.cluster.multicast-time-to-live=32
```

#### AWS EC2 Discovery

```properties
# {{ include.configKey }}.cluster.discovery.enabled=true

# {{ include.configKey }}.cluster.discovery.aws.access-ley=
# {{ include.configKey }}.cluster.discovery.aws.secret-ley=

# {{ include.configKey }}.cluster.discovery.aws.iam-role=

# {{ include.configKey }}.cluster.discovery.aws.region=us-east-1
# {{ include.configKey }}.cluster.discovery.aws.host-header=
# {{ include.configKey }}.cluster.discovery.aws.security-group-name=
# {{ include.configKey }}.cluster.discovery.aws.tag-key=
# {{ include.configKey }}.cluster.discovery.aws.tag-value=
# {{ include.configKey }}.cluster.discovery.aws.port=-1
# {{ include.configKey }}.cluster.discovery.aws.connection-timeout-seconds=5
```

### Apache jclouds Discovery

```properties
# {{ include.configKey }}.cluster.discovery.enabled=true

# {{ include.configKey }}.cluster.discovery.jclouds.provider=
# {{ include.configKey }}.cluster.discovery.jclouds.identity=
# {{ include.configKey }}.cluster.discovery.jclouds.credential=
# {{ include.configKey }}.cluster.discovery.jclouds.endpoint=
# {{ include.configKey }}.cluster.discovery.jclouds.zones=
# {{ include.configKey }}.cluster.discovery.jclouds.regions=
# {{ include.configKey }}.cluster.discovery.jclouds.tag-keys=
# {{ include.configKey }}.cluster.discovery.jclouds.tag-values=
# {{ include.configKey }}.cluster.discovery.jclouds.group=
# {{ include.configKey }}.cluster.discovery.jclouds.port=-1
# {{ include.configKey }}.cluster.discovery.jclouds.role-name=
# {{ include.configKey }}.cluster.discovery.jclouds.credential-path=
```

#### Kubernetes Discovery

```properties
# {{ include.configKey }}.cluster.discovery.enabled=true

# {{ include.configKey }}.service-dns=
# {{ include.configKey }}.service-dns-timeout=-1
# {{ include.configKey }}.service-name=
# {{ include.configKey }}.service-label-name=
# {{ include.configKey }}.service-label-value=
# {{ include.configKey }}.cluster.discovery.kubernetes.namespace=
# {{ include.configKey }}.resolve-not-ready-addresses=false
# {{ include.configKey }}.cluster.discovery.kubernetes.kubernetes-master=
# {{ include.configKey }}.api-token=
```

#### Docker Swarm Discovery

```properties
# {{ include.configKey }}.cluster.discovery.enabled=true

# {{ include.configKey }}.cluster.discovery.docker-swarm.dns-provider.enabled=true
# {{ include.configKey }}.cluster.discovery.docker-swarm.dns-provider.service-name=
# {{ include.configKey }}.cluster.discovery.docker-swarm.dns-provider.service-port=5701
# {{ include.configKey }}.cluster.discovery.docker-swarm.dns-provider.peer-services=service-a,service-b,etc

# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.enabled=true
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.group-name=
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.group-password=
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.docker-network-names=
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.docker-service-names=
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.docker-service-labels=
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.swarm-mgr-uri=
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.skip-verify-ssl=false
# {{ include.configKey }}.cluster.discovery.docker-swarm.member-provider.hazelcast-peer-port=5701
```

#### Microsoft Azure Discovery

{% capture cfgkey %}{{ include.configKey }}.cluster.discovery.azure{% endcapture %}
{% include casproperties.html properties=cfgkey %}
