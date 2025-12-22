package org.apereo.cas.pac4j.web.flow;

import module java.base;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.credentials.SessionKeyCredentials;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DelegatedClientOidcLogoutAction}.
 * <p>
 * The action takes into account the currently used PAC4J client which is stored
 * in the user profile. If the client is found, its logout action is executed.
 * <p>
 * Assumption: The PAC4J user profile should be in the user session during
 * logout, accessible with PAC4J Profile Manager. The Logout web flow should
 * make sure the user profile is present.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedClientOidcLogoutAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;
    private final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    private void removeSsoSessionsForSessionIndexes(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final SessionKeyCredentials sessionKeyCredentials) {
        val sessionKey = sessionKeyCredentials.getSessionKey();
        LOGGER.debug("Destroying SSO session for OIDC authn delegation for session key: [{}]", sessionKey);
        ticketRegistry
            .getSessionsWithAttributes(Map.of("sid", List.of(Objects.requireNonNull(sessionKey))))
            .filter(ticket -> !ticket.isExpired())
            .map(TicketGrantingTicket.class::cast)
            .findFirst()
            .ifPresent(ticket -> singleLogoutRequestExecutor.execute(ticket.getId(), request, response));
    }

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val clientCredential = WebUtils.getCredential(requestContext, ClientCredential.class);
        if (clientCredential != null && clientCredential.getCredentials() instanceof final SessionKeyCredentials sessionKeyCredentials) {
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            removeSsoSessionsForSessionIndexes(request, response, sessionKeyCredentials);
        }
        return success();
    }
}
