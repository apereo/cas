package org.apereo.cas.web.flow.actions.logout;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.springframework.http.HttpMethod;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link DelegatedSaml2ClientLogoutAction}.
 * <p>
 * The action takes into account the currently used PAC4J client which is stored
 * in the user profile. If the client is found, its logout action is executed.
 * <p>
 * Assumption: The PAC4J user profile should be in the user session during
 * logout, accessible with PAC4J Profile Manager. The Logout web flow should
 * make sure the user profile is present.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedSaml2ClientLogoutAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;
    private final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    private void removeSsoSessionsForSessionIndexes(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final LogoutRequest logoutRequest) {
        logoutRequest.getSessionIndexes().forEach(sessionIndex -> ticketRegistry
            .getSessionsWithAttributes(Map.of("sessionindex", List.of(Objects.requireNonNull(sessionIndex.getValue()))))
            .filter(ticket -> !ticket.isExpired())
            .map(TicketGrantingTicket.class::cast)
            .findFirst()
            .ifPresent(ticket -> singleLogoutRequestExecutor.execute(ticket.getId(), request, response)));
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val clientCredential = WebUtils.getCredential(requestContext, ClientCredential.class);
        if (clientCredential != null && clientCredential.getCredentials() instanceof final SAML2Credentials saml2Credentials
            && saml2Credentials.getContext().getMessageContext().getMessage() instanceof final LogoutRequest logoutRequest
            && HttpMethod.POST.matches(request.getMethod())) {
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            removeSsoSessionsForSessionIndexes(request, response, logoutRequest);
        }
        return success();
    }
}
