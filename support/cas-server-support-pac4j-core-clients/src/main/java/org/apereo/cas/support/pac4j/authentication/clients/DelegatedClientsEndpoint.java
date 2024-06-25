package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * This is {@link DelegatedClientsEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@Endpoint(id = "delegatedClients", enableByDefault = false)
public class DelegatedClientsEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<DelegatedIdentityProviderFactory> clientFactory;
    private final ObjectProvider<List<DelegatedClientsEndpointContributor>> delegatedClientsEndpointContributors;

    public DelegatedClientsEndpoint(final CasConfigurationProperties casProperties,
                                    final ObjectProvider<DelegatedIdentityProviderFactory> clientFactory,
                                    final ObjectProvider<List<DelegatedClientsEndpointContributor>> delegatedClientsEndpointContributors) {
        super(casProperties);
        this.clientFactory = clientFactory;
        this.delegatedClientsEndpointContributors = delegatedClientsEndpointContributors;
    }

    /**
     * Reload the identity providers and rebuild from scratch.
     *
     * @return the identity providers.
     */
    @DeleteOperation
    @Operation(summary = "Clear loaded identity providers and rebuild from CAS configuration or other sources.")
    public Map<String, Map<String, String>> reload() {
        val currentClients = clientFactory.getObject().rebuild();
        return buildClientMap(currentClients);
    }

    /**
     * Gets loaded delegated identity provider clients from the configuraton.
     *
     * @return the loaded clients
     */
    @ReadOperation
    @Operation(summary = "Load delegated identity provider clients from the configuration")
    public Map<String, Map<String, String>> getClients() {
        val currentClients = clientFactory.getObject().build();
        return buildClientMap(currentClients);
    }

    private Map<String, Map<String, String>> buildClientMap(final Collection<BaseClient> currentClients) {
        val clientsMap = new TreeMap<String, Map<String, String>>();
        currentClients.forEach(client -> {
            if (Objects.requireNonNull(client) instanceof final CasClient instance) {
                clientsMap.put(client.getName(), fetchCasConfiguration(instance.getConfiguration()));
            } else {
                delegatedClientsEndpointContributors.ifAvailable(contributors ->
                    contributors
                        .stream()
                        .filter(contributor -> contributor.supports(client))
                        .forEach(contributor -> clientsMap.put(client.getName(), contributor.contribute(client))));
            }
        });
        return clientsMap;
    }
    
    protected Map<String, String> fetchCasConfiguration(final CasConfiguration configuration) {
        return CollectionUtils.wrap("protocol", configuration.getProtocol(),
            "loginUrl", configuration.getLoginUrl());
    }
}
