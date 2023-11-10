package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.ticket.TicketGrantingTicket;
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
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext)
            .orElseThrow(() -> new IllegalArgumentException("Unable to determine delegated client name"));
        val client = configContext.getIdentityProviders().findClient(clientName)
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

            if (clientCredential.getCredentials() instanceof final SAML2Credentials saml2Credentials
                && saml2Credentials.getContext().getMessageContext().getMessage() instanceof final LogoutRequest logoutRequest) {
                removeSsoSessionsForSessionIndexes(request, response, logoutRequest);
            }
            return new Event(this, CasWebflowConstants.TRANSITION_ID_DONE);
        }
        return new Event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
    }

    private void removeSsoSessionsForSessionIndexes(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final LogoutRequest logoutRequest) {
        logoutRequest.getSessionIndexes().forEach(sessionIndex -> configContext.getTicketRegistry()
            .getSessionsWithAttributes(Map.of("sessionindex", List.of(Objects.requireNonNull(sessionIndex.getValue()))))
            .filter(ticket -> !ticket.isExpired())
            .map(TicketGrantingTicket.class::cast)
            .findFirst()
            .ifPresent(ticket -> configContext.getSingleLogoutRequestExecutor().execute(ticket.getId(), request, response)));

    }
}
