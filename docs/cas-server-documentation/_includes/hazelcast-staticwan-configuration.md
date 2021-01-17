#### Hazelcast Static WAN Replication

{% capture cfgkey %}{{ include.configKey }}.cluster.wan-replication{% endcapture %}
{% include casproperties.html properties=cfgkey %}
