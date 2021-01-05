There is no default value for the Terracota Cluster URI but 
the format is `terracotta://host1.company.org:9410,host2.company.org:9410/cas-application`

```properties
# cas.ticket.registry.ehcache3.enabled=true
# cas.ticket.registry.ehcache3.max-elements-in-memory=10000
# cas.ticket.registry.ehcache3.per-cache-size-on-disk=20MB
# cas.ticket.registry.ehcache3.eternal=false
# cas.ticket.registry.ehcache3.enable-statistics=true
# cas.ticket.registry.ehcache3.enable-management=true
# cas.ticket.registry.ehcache3.terracotta-cluster-uri=
# cas.ticket.registry.ehcache3.default-server-resource=main
# cas.ticket.registry.ehcache3.resource-pool-name=cas-ticket-pool
# cas.ticket.registry.ehcache3.resource-pool-size=15MB
# cas.ticket.registry.ehcache3.root-directory=/tmp/cas/ehcache3
# cas.ticket.registry.ehcache3.persist-on-disk=true
# cas.ticket.registry.ehcache3.cluster-connection-timeout=150
# cas.ticket.registry.ehcache3.cluster-read-write-timeout=5
# cas.ticket.registry.ehcache3.clustered-cache-consistency=STRONG
```   
