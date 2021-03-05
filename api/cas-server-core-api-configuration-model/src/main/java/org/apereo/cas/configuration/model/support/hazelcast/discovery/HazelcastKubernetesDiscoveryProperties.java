package org.apereo.cas.configuration.model.support.hazelcast.discovery;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HazelcastKubernetesDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-discovery-kubernetes")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HazelcastKubernetesDiscoveryProperties")
public class HazelcastKubernetesDiscoveryProperties implements Serializable {
    private static final long serialVersionUID = 8590530159392472509L;

    /**
     * Defines the DNS service lookup domain. This is defined as something similar
     * to {@code my-svc.my-namespace.svc.cluster.local}.
     */
    private String serviceDns;

    /**
     * Defines the DNS service lookup timeout in seconds. Defaults to 5 secs.
     */
    private int serviceDnsTimeout = -1;

    /**
     * Defines the service name of the POD to lookup through the Service Discovery REST API of Kubernetes.
     */
    private String serviceName;

    /**
     * Defines the service label to lookup through the Service Discovery REST API of Kubernetes.
     */
    private String serviceLabelName;

    /**
     * Defines the service label value to lookup through the Service Discovery REST API of Kubernetes.
     */
    private String serviceLabelValue;

    /**
     * Defines the namespace of the application POD through the Service Discovery REST API of Kubernetes.
     */
    private String namespace;

    /**
     *  Defines if not ready addresses should be evaluated to be discovered on startup.
     */
    private boolean resolveNotReadyAddresses;

    /**
     * Defines an alternative address for the kubernetes master. Defaults to: {@code https://kubernetes.default.svc}
     */
    private String kubernetesMaster;

    /**
     * Defines an oauth token for the kubernetes client to access the kubernetes REST API. Defaults to reading the
     * token from the auto-injected file at: {@code /var/run/secrets/kubernetes.io/serviceaccount/token}.
     */
    private String apiToken;

    /**
     * Defines the pod label to lookup through the Service Discovery REST API of Kubernetes.
     */
    private String podLabelName;

    /**
     * Defines the pod label value to lookup through the Service Discovery REST API of Kubernetes.
     */
    private String podLabelValue;

    /**
     * Defines if the node name should be used as external address, instead of looking up the external IP using
     * the {@code /nodes} resource. Default is false.
     */
    private boolean useNodeNameAsExternalAddress;

    /**
     * Defines the number of retries to Kubernetes API. Defaults to: 3.
     */
    private int apiRetries = 3;

    /**
     * CA Authority certificate from Kubernetes Master. Defaults to reading the certificate from the auto-injected file at:
     * {@code /var/run/secrets/kubernetes.io/serviceaccount/ca.crt}.
     */
    private String caCertificate;

    /**
     * If specified with a value greater than 0, its value defines the
     * endpoint port of the service (overriding the default).
     */
    private int servicePort;
}
