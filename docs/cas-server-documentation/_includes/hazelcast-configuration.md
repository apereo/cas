#### Hazelcast Configuration

{% capture cfgkey %}{{ include.configKey }}.core{% endcapture %}
{% include casproperties.html properties=cfgkey %}
   
#### Hazelcast Clusters

{% capture cfgkey %}{{ include.configKey }}.cluster.{% endcapture %}
{% include casproperties.html properties=cfgkey excludes="wan-replication,aws,azure,jclouds,swarm,kubernetes" %}
