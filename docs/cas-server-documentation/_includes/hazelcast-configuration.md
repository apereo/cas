#### Hazelcast Configuration

{% capture cfgkey %}{{ include.configKey }}.license-key,{{ include.configKey }}.enable-compression,{{ include.configKey }}.enable-management-center-scripting{% endcapture %}
{% include casproperties.html properties=cfgkey %}
   
#### Hazelcast Clusters

{% capture cfgkey %}{{ include.configKey }}.cluster.{% endcapture %}
{% include casproperties.html properties=cfgkey excludes="wan-replication,aws,azure,jclouds,swarm,kubernetes" %}
