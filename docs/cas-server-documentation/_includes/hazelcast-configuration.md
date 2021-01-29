#### Hazelcast Configuration

{% capture cfgkey %}{{ include.configKey }}.core{% endcapture %}
{% include casproperties.html properties=cfgkey %}
   
#### Hazelcast Clusters

{% capture cfgkey %}{{ include.configKey }}.cluster.core{% endcapture %}
{% include casproperties.html properties=cfgkey %}
