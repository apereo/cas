package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This is {@link ClusterTopologyEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Endpoint(id = "clusterTopology", defaultAccess = Access.NONE)
public class ClusterTopologyEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<ClusterTopologyManager> managers;
    private final ObjectProvider<DiscoveryClient> discoveryClientProvider;

    public ClusterTopologyEndpoint(final CasConfigurationProperties casProperties,
                                   final ConfigurableApplicationContext applicationContext,
                                   final ObjectProvider<ClusterTopologyManager> managers,
                                   final ObjectProvider<DiscoveryClient> discoveryClientProvider) {
        super(casProperties, applicationContext);
        this.managers = managers;
        this.discoveryClientProvider = discoveryClientProvider;
    }

    /**
     * Members list.
     *
     * @return the list
     */
    @GetMapping(
        path = "/topology",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "This endpoint returns a list of cluster members with details such as ID, status, etc")
    public List<ClusterMember> members() {
        return managers
            .orderedStream()
            .filter(BeanSupplier::isNotProxy)
            .flatMap(Unchecked.function(manager -> manager.discoverMembers().stream()))
            .collect(Collectors.toList());
    }

    /**
     * Discovery list.
     *
     * @return the list
     */
    @GetMapping(
        path = "/discovery",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "This endpoint returns a list of cluster members discovered via the discovery client")
    public List<ClusterMember> discovery() {
        val discoveryClient = discoveryClientProvider.getObject();
        return discoveryClient
            .getServices()
            .stream()
            .map(serviceId -> discoveryClientProvider.getIfAvailable().getInstances(serviceId))
            .flatMap(Collection::stream)
            .<ClusterMember>map(instance -> ClusterMember.builder()
                .owner(instance.getServiceId())
                .id(instance.getInstanceId())
                .address("%s:%s".formatted(instance.getHost(), instance.getPort()))
                .status(true)
                .attributes(Map.of(
                    "serviceId", instance.getServiceId(),
                    "uri", instance.getUri().toString(),
                    "metadata", Objects.requireNonNullElseGet(instance.getMetadata(), Map::of)
                ))
                .build())
            .toList();
    }
}
