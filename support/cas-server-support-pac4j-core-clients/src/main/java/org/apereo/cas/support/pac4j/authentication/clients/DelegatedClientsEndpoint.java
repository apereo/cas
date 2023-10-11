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
import org.pac4j.core.client.IndirectClient;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Collection;
import java.util.Map;
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
    private final DelegatedIdentityProviderFactory clientFactory;

    public DelegatedClientsEndpoint(final CasConfigurationProperties casProperties,
                                    final DelegatedIdentityProviderFactory clientFactory) {
        super(casProperties);
        this.clientFactory = clientFactory;
    }

    /**
     * Reload the identity providers and rebuild from scratch.
     *
     * @return the identity providers.
     */
    @DeleteOperation
    @Operation(summary = "Clear loaded identity providers and rebuild from CAS configuration or other sources.")
    public Map<String, Map<String, String>> reload() {
        val currentClients = clientFactory.rebuild();
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
        val currentClients = clientFactory.build();
        return buildClientMap(currentClients);
    }

    private Map<String, Map<String, String>> buildClientMap(final Collection<IndirectClient> currentClients) {
        val clientsMap = new TreeMap<String, Map<String, String>>();
        currentClients
            .stream()
            .map(IndirectClient.class::cast)
            .forEach(client -> {
                if (client instanceof final CasClient instance) {
                    clientsMap.put(client.getName(),
                        fetchCasConfiguration(instance.getConfiguration()));
                }
                if (client instanceof final SAML2Client instance) {
                    clientsMap.put(client.getName(),
                        fetchSaml2Configuration(instance.getConfiguration()));
                }
                if (client instanceof final OidcClient instance) {
                    clientsMap.put(client.getName(),
                        fetchOidcConfiguration(instance.getConfiguration()));
                }
                if (client instanceof final OAuth20Client instance) {
                    clientsMap.put(client.getName(),
                        fetchOauthConfiguration(instance.getConfiguration()));
                }
            });
        return clientsMap;
    }

    protected Map<String, String> fetchOauthConfiguration(final OAuth20Configuration configuration) {
        return CollectionUtils.wrap("clientId", configuration.getKey(),
            "responseType", configuration.getResponseType(),
            "scope", configuration.getScope());
    }

    protected Map<String, String> fetchOidcConfiguration(final OidcConfiguration configuration) {
        return CollectionUtils.wrap("clientId", configuration.getClientId(),
            "discoveryURI", configuration.getDiscoveryURI(),
            "responseType", configuration.getResponseType(),
            "scope", configuration.getScope());
    }

    protected Map<String, String> fetchSaml2Configuration(final SAML2Configuration configuration) {
        return CollectionUtils.wrap("serviceProviderEntityId", configuration.getServiceProviderEntityId(),
            "identityProviderEntityId", configuration.getIdentityProviderEntityId(),
            "identityProviderMetadata", configuration.getIdentityProviderMetadataResource().toString());
    }

    protected Map<String, String> fetchCasConfiguration(final CasConfiguration configuration) {
        return CollectionUtils.wrap("protocol", configuration.getProtocol(),
            "loginUrl", configuration.getLoginUrl());
    }
}
