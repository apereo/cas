#### Hazelcast Configuration

{% capture cfgkey %}{{ include.configKey }}.license-key,{{ include.configKey }}.enable-compression,{{ include.configKey }}.enable-management-center-scripting{% endcapture %}
{% include casproperties.html properties=cfgkey %}
   
#### Hazelcast Clusters

{% capture cfgkey %}{{ include.configKey }}.cluster.{% endcapture %}
{% include casproperties.html properties=cfgkey %}

#### Static WAN Replication

{% capture cfgkey %}{{ include.configKey }}.cluster.wan-replication{% endcapture %}
{% include casproperties.html properties=cfgkey %}

#### Multicast Discovery

{% capture cfgkey %}{{ include.configKey }}.cluster.multicast-{% endcapture %}
{% include casproperties.html properties=cfgkey %}

#### AWS EC2 Discovery

{% capture cfgkey %}{{ include.configKey }}.cluster.discovery.aws.{% endcapture %}
{% include casproperties.html properties=cfgkey %}

### Apache jclouds Discovery

{% capture cfgkey %}{{ include.configKey }}.cluster.discovery.jclouds.{% endcapture %}
{% include casproperties.html properties=cfgkey %}

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

{% capture cfgkey %}{{ include.configKey }}.cluster.discovery.docker-swarm.{% endcapture %}
{% include casproperties.html properties=cfgkey %}

#### Microsoft Azure Discovery

{% capture cfgkey %}{{ include.configKey }}.cluster.discovery.azure{% endcapture %}
{% include casproperties.html properties=cfgkey %}
