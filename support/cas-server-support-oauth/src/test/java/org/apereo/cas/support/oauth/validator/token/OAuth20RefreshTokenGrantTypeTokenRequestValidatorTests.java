package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20RefreshTokenGrantTypeTokenRequestValidatorTests {
    private TicketRegistry ticketRegistry;
    private OAuth20TokenRequestValidator validator;
    private OAuthRegisteredService registeredService;

    @Before
    public void before() {
        val oauthCode = mock(RefreshToken.class);
        when(oauthCode.getId()).thenReturn("RT-12345678");
        when(oauthCode.isExpired()).thenReturn(false);
        when(oauthCode.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());

        this.ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(anyString())).thenReturn(oauthCode);

        registeredService = new OAuthRegisteredService();
        registeredService.setName("OAuthRegisteredService");
        registeredService.setId(1);
        registeredService.setServiceId("https://www.example.org");
        registeredService.setClientId("client");
        registeredService.setServiceId("secret");
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.getAllServices()).thenReturn(CollectionUtils.wrapList(registeredService));

        this.validator = new OAuth20RefreshTokenGrantTypeTokenRequestValidator(
            new RegisteredServiceAccessStrategyAuditableEnforcer(), servicesManager,
            this.ticketRegistry, new WebApplicationServiceFactory());
    }

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("client");
        val session = request.getSession(true);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        val response = new MockHttpServletResponse();
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.setParameter(OAuth20Constants.CLIENT_ID, "client");
        request.setParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        request.setParameter(OAuth20Constants.REFRESH_TOKEN, "RT-12345678");


        registeredService.setSupportedGrantTypes(CollectionUtils.wrapHashSet(OAuth20GrantTypes.REFRESH_TOKEN.getType()));
        assertTrue(this.validator.validate(new J2EContext(request, response)));

        registeredService.setSupportedGrantTypes(CollectionUtils.wrapHashSet(OAuth20GrantTypes.PASSWORD.getType()));
        assertFalse(this.validator.validate(new J2EContext(request, response)));
    }
}
