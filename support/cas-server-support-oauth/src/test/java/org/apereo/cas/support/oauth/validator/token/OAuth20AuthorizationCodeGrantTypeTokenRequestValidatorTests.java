package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
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
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpSession;
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
    private static final String SUPPORTING_SERVICE_TICKET = "OC-SUPPORTING";
    private static final String NON_SUPPORTING_SERVICE_TICKET = "OC-NON-SUPPORTING";
    private static final String PROMISCUOUS_SERVICE_TICKET = "OC-PROMISCUOUS";

    private OAuth20TokenRequestValidator validator;

    private TicketRegistry ticketRegistry;

    private void registerTicket(final String name, final OAuthRegisteredService service) {
        final OAuth20CasAuthenticationBuilder builder = new OAuth20CasAuthenticationBuilder(
                new DefaultPrincipalFactory(),
                new WebApplicationServiceFactory(),
                new DefaultOAuth20ProfileScopeToAttributesFilter(),
                new CasConfigurationProperties());
        final Service oauthCasAuthenticationBuilderService = builder.buildService(service, null, false);
        final ExpirationPolicy expirationPolicy = new OAuthCodeExpirationPolicy(1, 60);
        final OAuthCode oauthCode = new DefaultOAuthCodeFactory(expirationPolicy).create(oauthCasAuthenticationBuilderService,
                RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"), new HashSet<>());
        when(ticketRegistry.getTicket(eq(name), any())).thenReturn(oauthCode);
    }

    @Before
    public void before() {
        final ServicesManager serviceManager = mock(ServicesManager.class);
        final OAuthRegisteredService supportingService = RequestValidatorTestUtils.getService(
                RegisteredServiceTestUtils.CONST_TEST_URL,
                RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        final OAuthRegisteredService nonSupportingService = RequestValidatorTestUtils.getService(
                RegisteredServiceTestUtils.CONST_TEST_URL2,
                RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET,
                CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        final OAuthRegisteredService promiscuousService = RequestValidatorTestUtils.getPromiscousService(
                RegisteredServiceTestUtils.CONST_TEST_URL3,
                RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
                RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
                RequestValidatorTestUtils.SHARED_SECRET);

        this.ticketRegistry = mock(TicketRegistry.class);

        registerTicket(SUPPORTING_SERVICE_TICKET, supportingService);
        registerTicket(NON_SUPPORTING_SERVICE_TICKET, nonSupportingService);
        registerTicket(PROMISCUOUS_SERVICE_TICKET, promiscuousService);

        when(serviceManager.getAllServices()).thenReturn(
                CollectionUtils.wrapList(
                        supportingService,
                        nonSupportingService,
                        promiscuousService));
        this.validator = new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(serviceManager,
                ticketRegistry, new RegisteredServiceAccessStrategyAuditableEnforcer(),
                new WebApplicationServiceFactory());
    }

    @Test
    public void verifyOperation() {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final CommonProfile profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        final HttpSession session = request.getSession(true);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);

        request.setParameter(OAuth20Constants.CODE, SUPPORTING_SERVICE_TICKET);
        assertTrue(this.validator.validate(new J2EContext(request, response)));

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(this.validator.validate(new J2EContext(request, response)));

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
        assertFalse(this.validator.validate(new J2EContext(request, response)));
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());

        request.setParameter(OAuth20Constants.CODE, NON_SUPPORTING_SERVICE_TICKET);
        request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL2);
        profile.setId(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);
        assertFalse(this.validator.validate(new J2EContext(request, response)));

        request.setParameter(OAuth20Constants.CODE, PROMISCUOUS_SERVICE_TICKET);
        profile.setId(RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL3);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);
        assertTrue(this.validator.validate(new J2EContext(request, response)));
    }
}
