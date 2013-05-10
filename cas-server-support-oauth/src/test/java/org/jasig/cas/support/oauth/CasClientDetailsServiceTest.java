package org.jasig.cas.support.oauth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

@RunWith(MockitoJUnitRunner.class)
public class CasClientDetailsServiceTest {
	
	@Mock private ServicesManager servicesManager;
	@Mock private RegisteredService registeredService;
	
	private CasClientDetailsService casClientDetailsService;
	private Collection<String> authorizedGrantTypes;
	
	@Before
	public void setUp() {
		this.authorizedGrantTypes = new ArrayList<String>();
		Collections.addAll(this.authorizedGrantTypes, 
				GrantType.AUTHORIZATION_CODE, 
				GrantType.CLIENT_CREDENTIALS, 
				GrantType.REFRESH_TOKEN);
		
		this.casClientDetailsService = new CasClientDetailsService(servicesManager, authorizedGrantTypes);
	}

	@Test(expected = ClientRegistrationException.class)
	public void testLoadClientByClientId_notFound() {
		Mockito.when(servicesManager.getAllServices()).thenReturn(new ArrayList<RegisteredService>());
		this.casClientDetailsService.loadClientByClientId("doesn't exist");
	}
	
	@Test
	public void testLoadClientId_found() {
		String clientId = "valid_client_id";
		String clientSecret = "valid_client_secret";
		
		Mockito.when(registeredService.getName()).thenReturn(clientId);
		Mockito.when(registeredService.getDescription()).thenReturn(clientSecret);
		Mockito.when(servicesManager.getAllServices()).thenReturn(Collections.singletonList(registeredService));
		
		ClientDetails clientDetails = this.casClientDetailsService.loadClientByClientId(clientId);
		Assert.assertNotNull(clientDetails);
		Assert.assertEquals(clientId, clientDetails.getClientId());
		Assert.assertEquals(clientSecret, clientDetails.getClientSecret());
		Assert.assertArrayEquals(this.authorizedGrantTypes.toArray(), clientDetails.getAuthorizedGrantTypes().toArray());
	}

}
