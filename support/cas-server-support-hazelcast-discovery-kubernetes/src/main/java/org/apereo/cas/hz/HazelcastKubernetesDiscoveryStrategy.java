package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import lombok.val;
import org.springframework.util.StringUtils;

import java.util.HashMap;

/**
 * This is {@link HazelcastKubernetesDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class HazelcastKubernetesDiscoveryStrategy implements HazelcastDiscoveryStrategy {

    @Override
    public DiscoveryStrategyConfig get(final HazelcastClusterProperties cluster, final JoinConfig joinConfig,
                                       final Config configuration, final NetworkConfig networkConfig) {
        val kube = cluster.getDiscovery().getKubernetes();
        val properties = new HashMap<String, Comparable>();

        if (StringUtils.hasText(kube.getServiceDns())) {
            properties.put(KubernetesProperties.SERVICE_DNS.key(), kube.getServiceDns());
        }
        if (kube.getServiceDnsTimeout() > 0) {
            properties.put(KubernetesProperties.SERVICE_DNS_TIMEOUT.key(), kube.getServiceDnsTimeout());
        }
        if (StringUtils.hasText(kube.getServiceName())) {
            properties.put(KubernetesProperties.SERVICE_NAME.key(), kube.getServiceName());
        }
        if (StringUtils.hasText(kube.getServiceLabelName())) {
            properties.put(KubernetesProperties.SERVICE_LABEL_NAME.key(), kube.getServiceLabelName());
        }
        if (StringUtils.hasText(kube.getServiceLabelValue())) {
            properties.put(KubernetesProperties.SERVICE_LABEL_VALUE.key(), kube.getServiceLabelValue());
        }
        if (StringUtils.hasText(kube.getNamespace())) {
            properties.put(KubernetesProperties.NAMESPACE.key(), kube.getNamespace());
        }
        if (StringUtils.hasText(kube.getKubernetesMaster())) {
            properties.put(KubernetesProperties.KUBERNETES_MASTER_URL.key(), kube.getKubernetesMaster());
        }
        if (StringUtils.hasText(kube.getApiToken())) {
            properties.put(KubernetesProperties.KUBERNETES_API_TOKEN.key(), kube.getApiToken());
        }
        properties.put(KubernetesProperties.RESOLVE_NOT_READY_ADDRESSES.key(), kube.isResolveNotReadyAddresses());

        return new DiscoveryStrategyConfig(new HazelcastKubernetesDiscoveryStrategyFactory(), properties);
    }

}
