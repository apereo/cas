package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.Before;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests {
    private OAuth20TokenRequestValidator validator;

    private TicketRegistry ticketRegistry;
    private OAuthRegisteredService registeredService;

    @Before
    public void before() {
        final var service = RegisteredServiceTestUtils.getService();

        final var serviceManager = mock(ServicesManager.class);
        registeredService = new OAuthRegisteredService();
        registeredService.setName("OAuth");
        registeredService.setClientId("client");
        registeredService.setClientSecret("secret");
        registeredService.setServiceId(service.getId());

        final var builder = new OAuth20CasAuthenticationBuilder(new DefaultPrincipalFactory(),
            new WebApplicationServiceFactory(), new DefaultOAuth20ProfileScopeToAttributesFilter(), new CasConfigurationProperties());
        final var oauthCasAuthenticationBuilderService = builder.buildService(registeredService, null, false);
        final ExpirationPolicy expirationPolicy = new OAuthCodeExpirationPolicy(1, 60);
        final var oauthCode = new DefaultOAuthCodeFactory(expirationPolicy).create(oauthCasAuthenticationBuilderService,
            RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"), new HashSet<>());

        this.ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(anyString(), any())).thenReturn(oauthCode);

        when(serviceManager.getAllServices()).thenReturn(CollectionUtils.wrapList(registeredService));
        this.validator = new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(serviceManager,
            ticketRegistry, new RegisteredServiceAccessStrategyAuditableEnforcer());
    }

    @Test
    public void verifyOperation() {
        final var service = RegisteredServiceTestUtils.getService();

        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(this.validator.validate(new J2EContext(request, response)));

        final var profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("client");
        final var session = request.getSession(true);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getId());
        request.setParameter(OAuth20Constants.CODE, "OC-12345678");
        assertTrue(this.validator.validate(new J2EContext(request, response)));
    }
}
