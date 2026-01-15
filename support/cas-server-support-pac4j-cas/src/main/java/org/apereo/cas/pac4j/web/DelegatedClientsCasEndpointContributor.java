package org.apereo.cas.pac4j.web;

import module java.base;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;

/**
 * This is {@link DelegatedClientsCasEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class DelegatedClientsCasEndpointContributor implements DelegatedClientsEndpointContributor {

    @Override
    public boolean supports(final BaseClient client) {
        return client instanceof CasClient;
    }

    @Override
    public Map<String, Object> contribute(final BaseClient client) {
        return fetchCasConfiguration(((CasClient) client).getConfiguration());
    }

    protected Map<String, Object> fetchCasConfiguration(final CasConfiguration configuration) {
        val payload = CollectionUtils.<String, Object>wrap(
            "protocol", configuration.getProtocol(),
            "loginUrl", configuration.getLoginUrl(),
            "timeTolerance", configuration.getTimeTolerance()
        );
        payload.putAll(configuration.getCustomParams());
        payload.put("type", "cas");
        return payload;
    }
}
