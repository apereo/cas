package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationIdentityProviderFinalizeLogoutAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedAuthenticationIdentityProviderFinalizeLogoutAction extends BaseCasWebflowAction {
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext).orElse(StringUtils.EMPTY);
        val client = configContext.getIdentityProviders().findClient(clientName).orElseThrow();
        LOGGER.debug("Received logout request from [{}]", client.getName());

        val redirectUrl = configContext.getCasProperties().getLogout().getRedirectParameter()
            .stream()
            .map(webContext::getRequestParameter)
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .filter(StringUtils::isNotBlank)
            .findFirst();
        redirectUrl.filter(StringUtils::isNotBlank).ifPresent(url -> {
            val builder = UriComponentsBuilder.fromHttpUrl(url);
            val logoutUrl = builder.build().toUriString();
            LOGGER.debug("Redirect URL after logout is: [{}]", logoutUrl);
            WebUtils.putLogoutRedirectUrl(request, logoutUrl);
        });
        request.getServletContext()
            .getRequestDispatcher(CasProtocolConstants.ENDPOINT_LOGOUT)
            .forward(request, response);
        requestContext.getExternalContext().recordResponseComplete();
        return null;
    }
}
