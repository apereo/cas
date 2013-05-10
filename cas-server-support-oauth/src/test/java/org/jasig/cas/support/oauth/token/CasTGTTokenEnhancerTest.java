/**
 * 
 */
package org.jasig.cas.support.oauth.token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

/**
 * @author joe
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CasTGTTokenEnhancerTest {
	
	@Mock private TicketRegistry casTicketRegistry;
	@Mock private OAuth2Authentication authentication;
	@Mock private TicketGrantingTicket ticketGrantingTicket;
	private TokenEnhancer tokenEnhancer;
	
	final private long accessTokenValiditySeconds = 7200;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TokenExpirationConfig config = new TokenExpirationConfig();
		config.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
		tokenEnhancer = new CasTGTTokenEnhancer(config, casTicketRegistry);
	}

	/**
	 * Test method for {@link org.jasig.cas.support.oauth.token.CasTGTTokenEnhancer#enhance(org.springframework.security.oauth2.common.OAuth2AccessToken, org.springframework.security.oauth2.provider.OAuth2Authentication)}.
	 */
	@Test
	public void testEnhance_noTGT() {
		Mockito.when(casTicketRegistry.getTickets()).thenReturn(new ArrayList<Ticket>());		
		Mockito.when(authentication.getName()).thenReturn("doesn't matter");
		
		OAuth2AccessToken inputAccessToken = new DefaultOAuth2AccessToken("this value shouldn't change");
		OAuth2AccessToken outputAccessToken = tokenEnhancer.enhance(inputAccessToken, authentication);
		
		Assert.assertEquals(inputAccessToken, outputAccessToken);
	}
	
	@Test
	public void testEnhance_foundTGT() {
		final String tgtValue = "TGT-" + UUID.randomUUID();
		final String validUsername = "jdoe@jasig.org";
		
		Authentication casAuthentication = Mockito.mock(Authentication.class);
		Mockito.when(casAuthentication.getPrincipal()).thenReturn(new SimplePrincipal(validUsername));
		Mockito.when(ticketGrantingTicket.getAuthentication()).thenReturn(casAuthentication);
		Mockito.when(ticketGrantingTicket.getId()).thenReturn(tgtValue);
		Mockito.when(ticketGrantingTicket.getCreationTime()).thenReturn(System.currentTimeMillis());
		
		Mockito.when(casTicketRegistry.getTickets()).thenReturn(Collections.singletonList((Ticket) ticketGrantingTicket));
		
		Mockito.when(authentication.getName()).thenReturn(validUsername);
		
		OAuth2AccessToken inputAccessToken = new DefaultOAuth2AccessToken("this value should change");
		
		final long startTime = System.currentTimeMillis();

		OAuth2AccessToken outputAccessToken = tokenEnhancer.enhance(inputAccessToken, authentication);
		
		Assert.assertEquals(tgtValue, outputAccessToken.getValue());
		Assert.assertEquals(accessTokenValiditySeconds, outputAccessToken.getExpiresIn(), 1);
		Assert.assertEquals(startTime + TimeUnit.SECONDS.toMillis(accessTokenValiditySeconds),
				outputAccessToken.getExpiration().getTime(),
							500);
		
	}

}
