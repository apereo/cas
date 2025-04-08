package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.action.EventFactorySupport;
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
        val client = configContext.getIdentityProviders().findClient(clientName, webContext).orElseThrow();
        LOGGER.debug("Received logout request from [{}]", client.getName());
        var redirectUrl = configContext.getCasProperties().getLogout().getRedirectParameter()
            .stream()
            .map(webContext::getRequestParameter)
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(redirectUrl)) {
            val builder = UriComponentsBuilder.fromUriString(redirectUrl);
            val logoutUrl = builder.build().toUriString();
            LOGGER.debug("Redirect URL after logout is: [{}]", logoutUrl);
            WebUtils.putLogoutRedirectUrl(request, logoutUrl);
        } else {
            val logoutRequest = DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(requestContext,
                DelegatedAuthenticationClientLogoutRequest.class);
            if (logoutRequest != null && StringUtils.isNotBlank(logoutRequest.getTarget())) {
                WebUtils.putLogoutRedirectUrl(request, logoutRequest.getTarget());
            }
        }
        webContext.getRequestAttribute(SingleLogoutContinuation.class.getName(), SingleLogoutContinuation.class)
            .ifPresent(continuation ->
                requestContext.getConversationScope().put(SingleLogoutContinuation.class.getName(), continuation));
        WebUtils.removeCredential(requestContext);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_LOGOUT);
    }

}
