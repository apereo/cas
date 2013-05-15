package org.jasig.cas.support.oauth.token;

import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;

import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * @author Joe McCall
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CasTicketRegistryTokenStoreTest {

    private CasTicketRegistryTokenStore casTicketRegistryTokenStore;

    @Mock private TicketRegistry ticketRegistry;
    @Mock private TokenExpirationConfig tokenExpirationConfig;
    @Mock private TicketGrantingTicket ticket;

    private static final String TGT_VALUE = "TGT-1";
    private static final String CAS_USERNAME = "jdoe@jasig.org";
    private static final long TOKEN_VALIDITY_SECONDS = 7200L;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        Mockito.when(tokenExpirationConfig.getAccessTokenValiditySeconds()).thenReturn(TOKEN_VALIDITY_SECONDS);

        casTicketRegistryTokenStore = new CasTicketRegistryTokenStore();
        casTicketRegistryTokenStore.setTokenExpirationConfig(tokenExpirationConfig);
        casTicketRegistryTokenStore.setTicketRegistry(ticketRegistry);

        Mockito.when(ticket.getId()).thenReturn(TGT_VALUE);
        Mockito.when(ticket.getCreationTime()).thenReturn(System.currentTimeMillis());
        Mockito.when(ticketRegistry.getTicket(TGT_VALUE, TicketGrantingTicket.class)).thenReturn(ticket);
    }

    @Ignore("method not yet implemented")
    @Test
    public void testReadAuthenticationOAuth2AccessToken() {
        fail("Not yet implemented");
    }

    @Ignore("method not yet implemented")
    @Test
    public void testReadAuthenticationString() {
        fail("Not yet implemented");
    }

    @Test
    public void testStoreAccessToken() {
        // token is already stored, so make sure our implementation doesn't try to do more extra work
        casTicketRegistryTokenStore.storeAccessToken(null, null);
        Mockito.verify(ticketRegistry, Mockito.never()).addTicket(null);
    }

    @Test
    public void testReadAccessToken() {
        OAuth2AccessToken accessToken = casTicketRegistryTokenStore.readAccessToken(TGT_VALUE);

        final long deltaInSeconds = 1L;

        Assert.assertNotNull(accessToken);
        Assert.assertEquals(TGT_VALUE, accessToken.getValue());
        Assert.assertEquals(TOKEN_VALIDITY_SECONDS, accessToken.getExpiresIn(), deltaInSeconds);
    }

    @Test
    public void testRemoveAccessToken() {

        OAuth2AccessToken mockToken = Mockito.mock(OAuth2AccessToken.class);
        Mockito.when(mockToken.getValue()).thenReturn(TGT_VALUE);

        casTicketRegistryTokenStore.removeAccessToken(mockToken);

        Mockito.verify(ticketRegistry).deleteTicket(TGT_VALUE);
    }

    @Ignore("Refresh tokens not yet implemented")
    @Test
    public void testStoreRefreshToken() {
        fail("Not yet implemented");
    }


    @Ignore("Refresh tokens not yet implemented")
    @Test
    public void testReadRefreshToken() {
        fail("Not yet implemented");
    }


    @Ignore("Refresh tokens not yet implemented")
    @Test
    public void testReadAuthenticationForRefreshToken() {
        fail("Not yet implemented");
    }


    @Ignore("Refresh tokens not yet implemented")
    @Test
    public void testRemoveRefreshToken() {
        fail("Not yet implemented");
    }


    @Ignore("Refresh tokens not yet implemented")
    @Test
    public void testRemoveAccessTokenUsingRefreshToken() {
        fail("Not yet implemented");
    }


    @Test
    public void testGetAccessToken() {

        OAuth2Authentication mockAuthentication = Mockito.mock(OAuth2Authentication.class);

        // Test for when called with an unsupported authorization
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(null);
        OAuth2AccessToken shouldBeNullToken = casTicketRegistryTokenStore.getAccessToken(mockAuthentication);
        Assert.assertNull(shouldBeNullToken);

        final SimplePrincipal simplePrincipal = new SimplePrincipal(CAS_USERNAME);

        // Test for when called with no user found
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(simplePrincipal);
        OAuth2AccessToken notFoundToken = casTicketRegistryTokenStore.getAccessToken(mockAuthentication);
        Assert.assertNull(notFoundToken);

        // Test for when the user is logged in
        Mockito.when(ticket.getAuthentication()).thenReturn(new ImmutableAuthentication(simplePrincipal));
        Mockito.when(ticketRegistry.getTickets()).thenReturn(Collections.singletonList((Ticket) ticket));
        OAuth2AccessToken foundToken = casTicketRegistryTokenStore.getAccessToken(mockAuthentication);
        Assert.assertNotNull(foundToken);
        Assert.assertEquals(TGT_VALUE, foundToken.getValue());

        // For now we have no support for multiple tickets per user, so there is no need to make a test for other
        // fields in the authentication object
    }

    @Test
    public void testFindTokensByUserName() {

        final String notFoundUsername = "should not find this username";

        Collection<OAuth2AccessToken> shouldBeEmptyCollection =
                casTicketRegistryTokenStore.findTokensByUserName(notFoundUsername);
        Assert.assertNotNull(shouldBeEmptyCollection);
        Assert.assertTrue(shouldBeEmptyCollection.isEmpty());

        final SimplePrincipal simplePrincipal = new SimplePrincipal(CAS_USERNAME);

        // Should only be returning a list of one ticket
        Mockito.when(ticket.getAuthentication()).thenReturn(new ImmutableAuthentication(simplePrincipal));
        Mockito.when(ticketRegistry.getTickets()).thenReturn(Collections.singletonList((Ticket) ticket));
        Collection<OAuth2AccessToken> foundTokens =
                casTicketRegistryTokenStore.findTokensByUserName(CAS_USERNAME);
        Assert.assertNotNull(foundTokens);
        Assert.assertFalse(foundTokens.isEmpty());
        Assert.assertEquals(1, foundTokens.size());
        for (OAuth2AccessToken token: foundTokens) {
            Assert.assertEquals(TGT_VALUE, token.getValue());
        }
    }


    @Ignore("There is no modelling of clients in CAS")
    @Test
    public void testFindTokensByClientId() {
        fail("Not yet implemented");
    }

}
