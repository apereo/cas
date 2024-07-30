package org.apereo.cas.pac4j.web;

import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import java.util.Map;

/**
 * This is {@link DelegatedClientsOidcEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class DelegatedClientsOidcEndpointContributor implements DelegatedClientsEndpointContributor {

    @Override
    public boolean supports(final BaseClient client) {
        return client instanceof OAuth20Client || client instanceof OidcClient;
    }

    @Override
    public Map<String, Object> contribute(final BaseClient client) {
        if (client instanceof final OAuth20Client clientInstance) {
            return fetchOauthConfiguration(clientInstance.getConfiguration());
        }
        return fetchOidcConfiguration(((OidcClient) client).getConfiguration());
    }

    protected Map<String, Object> fetchOauthConfiguration(final OAuth20Configuration configuration) {
        val payload = CollectionUtils.<String, Object>wrap(
            "clientId", configuration.getKey(),
            "responseType", configuration.getResponseType(),
            "scope", configuration.getScope());
        payload.putAll(configuration.getCustomParams());
        payload.put("type", "oauth2");
        return payload;
    }

    protected Map<String, Object> fetchOidcConfiguration(final OidcConfiguration configuration) {
        val payload = CollectionUtils.<String, Object>wrap(
            "clientId", configuration.getClientId(),
            "discoveryURI", configuration.getDiscoveryURI(),
            "responseType", configuration.getResponseType(),
            "responseMode", configuration.getResponseMode(),
            "scope", configuration.getScope());
        payload.putAll(configuration.getCustomParams());
        payload.put("mappedClaims", configuration.getMappedClaims());
        payload.put("type", "oidc");
        return payload;
    }
}
