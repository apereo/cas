package org.apereo.cas.oidc.web.flow.account;

import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryQueryCriteria;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OidcAccountProfileAccessTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcAccountProfileAccessTokenAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val tgt = WebUtils.getTicketGrantingTicket(requestContext);
        if (tgt instanceof final AuthenticationAwareTicket aat) {
            val criteria = TicketRegistryQueryCriteria
                .builder()
                .type(OAuth20AccessToken.PREFIX)
                .decode(Boolean.TRUE)
                .principal(aat.getAuthentication().getPrincipal().getId())
                .build();
            val accessTokens = ticketRegistry.query(criteria)
                .stream()
                .map(OAuth20AccessToken.class::cast)
                .map(AccountOAuth20AccessToken::new)
                .toList();
            requestContext.getFlowScope().put("oidcAccessTokens", accessTokens);
        }
        return null;
    }
}
