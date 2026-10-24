package org.apereo.cas.oidc.web.flow.account;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcAccountProfileRemoveAccessTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDCWeb")
class OidcAccountProfileRemoveAccessTokenActionTests extends AbstractOidcTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_OIDC_ACCESS_TOKEN)
    private Action oidcAccountProfileRemoveAccessTokensAction;

    @Test
    void verifyOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
        val accessToken = getAccessToken(principal);
        ticketRegistry.addTicket(accessToken);
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(accessToken.getAuthentication()));
        context.setParameter("id", accessToken.getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            oidcAccountProfileRemoveAccessTokensAction.execute(context).getId());
        assertNull(ticketRegistry.getTicket(accessToken.getId()));
    }

    @Test
    void verifyTokenOfAnotherPrincipalIsKept() throws Throwable {
        val accessToken = getAccessToken(RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString()));
        ticketRegistry.addTicket(accessToken);
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(UUID.randomUUID().toString()));
        context.setParameter("id", accessToken.getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR,
            oidcAccountProfileRemoveAccessTokensAction.execute(context).getId());
        assertNotNull(ticketRegistry.getTicket(accessToken.getId()));
    }

    @Test
    void verifyRefreshTokenRevokesAccessTokens() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
        val accessToken = getAccessToken(principal);
        ticketRegistry.addTicket(accessToken);
        val authentication = accessToken.getAuthentication();
        val accessTokenId = accessToken.getId();
        val refreshTokenId = OAuth20RefreshToken.PREFIX + '-' + UUID.randomUUID();
        val refreshToken = mock(OAuth20RefreshToken.class);
        when(refreshToken.getId()).thenReturn(refreshTokenId);
        when(refreshToken.getPrefix()).thenReturn(OAuth20RefreshToken.PREFIX);
        when(refreshToken.getAuthentication()).thenReturn(authentication);
        when(refreshToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(refreshToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(refreshToken.getAccessTokens()).thenReturn(new HashSet<>(Set.of(accessTokenId)));
        ticketRegistry.addTicket(refreshToken);

        val context = MockRequestContext.create(applicationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(accessToken.getAuthentication()));
        context.setParameter("id", refreshTokenId);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            oidcAccountProfileRemoveAccessTokensAction.execute(context).getId());
        assertNull(ticketRegistry.getTicket(refreshTokenId));
        assertNull(ticketRegistry.getTicket(accessToken.getId()));
    }
}
