# Hazelcast Ticket Registry - Kubernetes Auto Discovery

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry-AutoDiscovery-Kubernetes.html

This hazelcast discovery plugin provides the possibility to lookup IP addresses of other members by resolving those requests against a [Kubernetes](https://kubernetes.io/) Service Discovery system.

This module supports two different options of resolving against the discovery registry:

- A request to the REST API
- DNS Lookup against a given headless DNS service name

See [this link](https://github.com/hazelcast/hazelcast-kubernetes) for more info.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-hazelcast-discovery-kubernetes"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.api-retries` (Optional; type `Integer`; default `3`): Defines the number of retries to Kubernetes API. Defaults to: 3.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.api-token` (Optional; type `String`): Defines an oauth token for the kubernetes client to access the kubernetes REST API. Defaults to reading the token from the auto-injected file at: `/var/run/secrets/kubernetes.io/serviceaccount/token`.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.ca-certificate` (Optional; type `String`): CA Authority certificate from Kubernetes Master. Defaults to reading the certificate from the auto-injected file at: `/var/run/secrets/kubernetes.io/serviceaccount/ca.crt`.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.kubernetes-master` (Optional; type `String`): Defines an alternative address for the kubernetes master. Defaults to: `https://kubernetes.default.svc`
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.namespace` (Optional; type `String`): Defines the namespace of the application POD through the Service Discovery REST API of Kubernetes.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.pod-label-name` (Optional; type `String`): Defines the pod label to lookup through the Service Discovery REST API of Kubernetes.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.pod-label-value` (Optional; type `String`): Defines the pod label value to lookup through the Service Discovery REST API of Kubernetes.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.resolve-not-ready-addresses` (Optional; type `Boolean`; default `false`): Defines if not ready addresses should be evaluated to be discovered on startup.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.service-dns` (Optional; type `String`): Defines the DNS service lookup domain. This is defined as something similar to `my-svc.my-namespace.svc.cluster.local`.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.service-dns-timeout` (Optional; type `Integer`; default `-1`): Defines the DNS service lookup timeout in seconds. Defaults to 5 secs.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.service-label-name` (Optional; type `String`): Defines the service label to lookup through the Service Discovery REST API of Kubernetes.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.service-label-value` (Optional; type `String`): Defines the service label value to lookup through the Service Discovery REST API of Kubernetes.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.service-name` (Optional; type `String`): Defines the service name of the POD to lookup through the Service Discovery REST API of Kubernetes.
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.service-port` (Optional; type `Integer`; default `0`): If specified with a value greater than 0, its value defines the endpoint port of the service (overriding the default).
- `cas.ticket.registry.hazelcast.cluster.discovery.kubernetes.use-node-name-as-external-address` (Optional; type `Boolean`; default `false`): Defines if the node name should be used as external address, instead of looking up the external IP using the `/nodes` resource. Default is false.
