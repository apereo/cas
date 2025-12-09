package org.apereo.cas.web.saml2;

import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientSessionManager;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link DelegatedClientSaml2SessionManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientSaml2SessionManager implements DelegatedClientSessionManager {
    private final ObjectProvider<@NonNull DelegatedClientAuthenticationConfigurationContext> contextProvider;

    @Override
    public void trackIdentifier(final WebContext webContext, final TransientSessionTicket ticket, final Client client) {
        contextProvider.getObject().getSessionStore().set(webContext,
            SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, ticket.getId());
    }

    @Override
    public String retrieveIdentifier(final WebContext webContext, final Client client) {
        return webContext.getRequestParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE)
            .or(() -> contextProvider.getObject().getSessionStore().get(webContext, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE).map(Object::toString))
            .orElse(StringUtils.EMPTY);
    }

    @Override
    public boolean supports(final Client client) {
        return client instanceof SAML2Client;
    }
}
