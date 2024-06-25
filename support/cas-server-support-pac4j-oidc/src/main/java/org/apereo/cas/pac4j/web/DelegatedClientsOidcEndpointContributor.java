package org.apereo.cas.pac4j.web;

import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.util.CollectionUtils;
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
    public Map<String, String> contribute(final BaseClient client) {
        if (client instanceof final OAuth20Client clientInstance) {
            return fetchOauthConfiguration(clientInstance.getConfiguration());
        }
        return fetchOidcConfiguration(((OidcClient) client).getConfiguration());
    }

    protected Map<String, String> fetchOauthConfiguration(final OAuth20Configuration configuration) {
        return CollectionUtils.wrap(
            "clientId", configuration.getKey(),
            "responseType", configuration.getResponseType(),
            "scope", configuration.getScope());
    }

    protected Map<String, String> fetchOidcConfiguration(final OidcConfiguration configuration) {
        return CollectionUtils.wrap(
            "clientId", configuration.getClientId(),
            "discoveryURI", configuration.getDiscoveryURI(),
            "responseType", configuration.getResponseType(),
            "responseMode", configuration.getResponseMode(),
            "scope", configuration.getScope());
    }
}
