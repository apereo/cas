package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedAuthenticationClientRetryAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class DelegatedAuthenticationClientRetryAction extends AbstractAction {
    private final Clients clients;

    private final DelegatedClientIdentityProviderConfigurationProducer providerConfigurationProducer;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val clientName = WebUtils.getDelegatedAuthenticationClientName(requestContext);
        val client = clients.findClient(clientName).map(IndirectClient.class::cast).get();
        val config = providerConfigurationProducer.produce(requestContext, client).get();

        val urlBuilder = new URIBuilder(config.getRedirectUrl());
        urlBuilder.addParameter(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, Boolean.TRUE.toString());
        response.sendRedirect(urlBuilder.toString());
        return null;
    }
}
