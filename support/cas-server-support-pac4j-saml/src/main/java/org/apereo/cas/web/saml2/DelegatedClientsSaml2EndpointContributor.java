package org.apereo.cas.web.saml2;

import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientsEndpointContributor;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.saml.client.SAML2Client;
import java.util.Map;

/**
 * This is {@link DelegatedClientsSaml2EndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class DelegatedClientsSaml2EndpointContributor implements DelegatedClientsEndpointContributor {

    @Override
    public boolean supports(final BaseClient client) {
        return client instanceof SAML2Client;
    }

    @Override
    public Map<String, String> contribute(final BaseClient client) {
        val saml2Client = (SAML2Client) client;
        saml2Client.init();
        val configuration = saml2Client.getConfiguration();
        var identityProviderEntityId = configuration.getIdentityProviderEntityId();
        if (StringUtils.isBlank(identityProviderEntityId)) {
            val identityProviderMetadataResolver = configuration.getIdentityProviderMetadataResolver();
            identityProviderMetadataResolver.resolve();
            identityProviderEntityId = identityProviderMetadataResolver.getEntityId();
        }
        return CollectionUtils.wrap(
            "serviceProviderEntityId", configuration.getServiceProviderEntityId(),
            "identityProviderEntityId", identityProviderEntityId,
            "identityProviderMetadata", configuration.getIdentityProviderMetadataResource().toString());
    }
}
