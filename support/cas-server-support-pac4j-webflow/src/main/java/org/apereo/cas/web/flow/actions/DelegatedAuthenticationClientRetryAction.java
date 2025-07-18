package org.apereo.cas.web.flow.actions;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedAuthenticationClientRetryAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class DelegatedAuthenticationClientRetryAction extends BaseCasWebflowAction {
    private final DelegatedIdentityProviders identityProviders;

    private final DelegatedClientIdentityProviderConfigurationProducer providerConfigurationProducer;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val clientName = DelegationWebflowUtils.getDelegatedAuthenticationClientName(requestContext);

        val webContext = new JEEContext(request, response);
        val client = identityProviders.findClient(clientName, webContext).map(IndirectClient.class::cast).orElseThrow();
        val config = providerConfigurationProducer.produce(requestContext, client).orElseThrow();

        val urlBuilder = new URIBuilder(config.getRedirectUrl());
        urlBuilder.addParameter(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, Boolean.TRUE.toString());
        response.sendRedirect(urlBuilder.toString());
        return null;
    }
}
