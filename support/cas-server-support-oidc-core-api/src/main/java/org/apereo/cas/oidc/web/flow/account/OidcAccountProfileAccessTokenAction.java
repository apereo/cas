package org.apereo.cas.oidc.web.flow.account;

import module java.base;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryQueryCriteria;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OidcAccountProfileAccessTokenAction}.
 * Lists access tokens and refresh tokens (offline access) issued to the authenticated principal.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcAccountProfileAccessTokenAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val tgt = WebUtils.getTicketGrantingTicket(requestContext);
        val authentication = tgt instanceof final AuthenticationAwareTicket aat ? aat.getAuthentication() : null;
        if (authentication != null) {
            val principal = authentication.getPrincipal().getId();
            val tokens = Stream.of(OAuth20AccessToken.PREFIX, OAuth20RefreshToken.PREFIX)
                .map(prefix -> TicketRegistryQueryCriteria.builder()
                    .type(prefix)
                    .decode(Boolean.TRUE)
                    .principal(principal)
                    .build())
                .flatMap(criteria -> ticketRegistry.query(criteria).stream())
                .filter(OAuth20Token.class::isInstance)
                .map(OAuth20Token.class::cast)
                .map(AccountOAuth20AccessToken::new)
                .toList();
            requestContext.getFlowScope().put("oidcAccessTokens", tokens);
        }
        return null;
    }
}
