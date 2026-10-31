package org.apereo.cas.oidc.web.flow.account;

import module java.base;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OidcAccountProfileRemoveAccessTokenAction}.
 * Revokes an access token or a refresh token that belongs to the authenticated principal;
 * revoking a refresh token also revokes the access tokens issued from it.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcAccountProfileRemoveAccessTokenAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val tokenId = requestContext.getRequestParameters().getRequired("id");
        val principal = getPrincipalId(WebUtils.getTicketGrantingTicket(requestContext));
        val ticket = ticketRegistry.getTicket(tokenId);
        if (principal == null || !(ticket instanceof final OAuth20Token token) || !principal.equals(getPrincipalId(token))) {
            LOGGER.warn("Token [{}] does not belong to the authenticated principal and cannot be revoked", tokenId);
            return error();
        }
        if (token instanceof final OAuth20RefreshToken refreshToken) {
            for (val accessToken : refreshToken.getAccessTokens()) {
                ticketRegistry.deleteTicket(accessToken);
            }
        }
        ticketRegistry.deleteTicket(tokenId);
        return success();
    }

    private static @Nullable String getPrincipalId(final @Nullable Object ticket) {
        if (ticket instanceof final AuthenticationAwareTicket aat) {
            val authentication = aat.getAuthentication();
            return authentication != null ? authentication.getPrincipal().getId() : null;
        }
        return null;
    }
}
