package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext)
            .orElseThrow(() -> new IllegalArgumentException("Unable to determine delegated client name"));
        val client = configContext.getIdentityProviders().findClient(clientName, webContext)
            .orElseThrow(() -> new IllegalArgumentException("Unable to determine delegated client for " + clientName));
        LOGGER.debug("Received logout request from [{}]", client.getName());

        val clientCredential = WebUtils.getCredential(requestContext, ClientCredential.class);
        if (clientCredential != null && HttpMethod.POST.matches(request.getMethod())) {
            webContext.getRequestAttribute(SingleLogoutContinuation.class.getName(), SingleLogoutContinuation.class)
                .stream()
                .filter(continuation -> StringUtils.isNotBlank(continuation.getUrl()))
                .findFirst()
                .ifPresent(continuation -> {
                    val exec = HttpExecutionRequest.builder()
                        .method(continuation.getMethod())
                        .url(continuation.getUrl())
                        .parameters(continuation.getData())
                        .build();
                    LOGGER.debug("Sending delegated logout response to [{}]", exec.getUrl());
                    val logoutResponse = HttpUtils.execute(exec);
                    FunctionUtils.doIf(logoutResponse == null || HttpStatus.valueOf(logoutResponse.getCode()).isError(),
                        r -> LOGGER.warn("Submitting logout response to [{}] failed with response [{}]", continuation.getUrl(), r)).accept(logoutResponse);
                    request.removeAttribute(SingleLogoutContinuation.class.getName());
                });
        }
        return new Event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
    }
}
