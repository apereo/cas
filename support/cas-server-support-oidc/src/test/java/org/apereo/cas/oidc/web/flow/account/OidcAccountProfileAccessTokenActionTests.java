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
 * This is {@link OidcAccountProfileAccessTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDCWeb")
class OidcAccountProfileAccessTokenActionTests extends AbstractOidcTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_ACCESS_TOKENS)
    private Action oidcAccountProfileAccessTokensAction;

    @Test
    void verifyOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
        val accessToken = getAccessToken(principal);
        ticketRegistry.addTicket(accessToken);
        val tgt = new MockTicketGrantingTicket(accessToken.getAuthentication());
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertNull(oidcAccountProfileAccessTokensAction.execute(context));
        val oidcAccessTokens = context.getFlowScope().get("oidcAccessTokens", List.class);
        assertNotNull(oidcAccessTokens);
        assertEquals(1, oidcAccessTokens.size());
    }
    @Test
    void verifyRefreshTokensAreListed() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
        val accessToken = getAccessToken(principal);
        val authentication = accessToken.getAuthentication();
        val service = accessToken.getService();
        val accessTokenId = accessToken.getId();
        val refreshToken = mock(OAuth20RefreshToken.class);
        when(refreshToken.getId()).thenReturn(OAuth20RefreshToken.PREFIX + '-' + UUID.randomUUID());
        when(refreshToken.getPrefix()).thenReturn(OAuth20RefreshToken.PREFIX);
        when(refreshToken.getAuthentication()).thenReturn(authentication);
        when(refreshToken.getService()).thenReturn(service);
        when(refreshToken.getClientId()).thenReturn("clientid");
        when(refreshToken.getScopes()).thenReturn(Set.of("openid", "offline_access"));
        when(refreshToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(refreshToken.getAccessTokens()).thenReturn(Set.of(accessTokenId));
        ticketRegistry.addTicket(refreshToken);

        val context = MockRequestContext.create(applicationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(accessToken.getAuthentication()));
        assertNull(oidcAccountProfileAccessTokensAction.execute(context));
        val tokens = context.getFlowScope().get("oidcAccessTokens", List.class);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        val token = (AccountOAuth20AccessToken) tokens.getFirst();
        assertTrue(token.isRefreshToken());
        assertEquals(1, token.getAccessTokenCount());
    }
}
