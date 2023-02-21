package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedAuthenticationIdentityProviderLogoutAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedAuthenticationIdentityProviderLogoutAction extends BaseCasWebflowAction {
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext)
            .orElseThrow(() -> new IllegalArgumentException("Unable to determine delegated client name"));
        val client = configContext.getClients().findClient(clientName)
            .orElseThrow(() -> new IllegalArgumentException("Unable to determine delegated client for " + clientName));
        LOGGER.debug("Received logout request from [{}]", client.getName());
        return new Event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
    }
}
