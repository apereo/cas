package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
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
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeExpirationPolicy;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
@DirtiesContext
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests extends AbstractOAuth20Tests {
    private static final String SUPPORTING_SERVICE_TICKET = "OC-SUPPORTING";

    private static final String NON_SUPPORTING_SERVICE_TICKET = "OC-NON-SUPPORTING";

    private static final String PROMISCUOUS_SERVICE_TICKET = "OC-PROMISCUOUS";

    private OAuth20TokenRequestValidator validator;

    private TicketRegistry mockingTicketRegistry;

    private void registerTicket(final String name, final OAuthRegisteredService service) {
        val builder = new OAuth20CasAuthenticationBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(),
            new WebApplicationServiceFactory(),
            new DefaultOAuth20ProfileScopeToAttributesFilter(),
            new CasConfigurationProperties());
        val oauthCasAuthenticationBuilderService = builder.buildService(service, null, false);
        val expirationPolicy = new ExpirationPolicyBuilder() {
            private static final long serialVersionUID = 3911344031977989503L;

            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return new OAuth20CodeExpirationPolicy(1, 60);
            }

            @Override
            public Class getTicketType() {
                return OAuth20Code.class;
            }
        };

        val oauthCode = new OAuth20DefaultOAuthCodeFactory(expirationPolicy, mock(ServicesManager.class))
            .create(oauthCasAuthenticationBuilderService, RegisteredServiceTestUtils.getAuthentication(),
                new MockTicketGrantingTicket("casuser"), new HashSet<>(),
                null, null, "clientid12345",
                new HashMap<>());
        when(mockingTicketRegistry.getTicket(eq(name), (Class<Ticket>) any())).thenReturn(oauthCode);
    }

    @BeforeEach
    public void before() {
        val serviceManager = mock(ServicesManager.class);
        val supportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL,
            RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.AUTHORIZATION_CODE));
        val nonSupportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL2,
            RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.PASSWORD));
        val promiscuousService = RequestValidatorTestUtils.getPromiscuousService(
            RegisteredServiceTestUtils.CONST_TEST_URL3,
            RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
            RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET);

        this.mockingTicketRegistry = mock(TicketRegistry.class);

        registerTicket(SUPPORTING_SERVICE_TICKET, supportingService);
        registerTicket(NON_SUPPORTING_SERVICE_TICKET, nonSupportingService);
        registerTicket(PROMISCUOUS_SERVICE_TICKET, promiscuousService);

        when(serviceManager.getAllServices()).thenReturn(
            CollectionUtils.wrapList(
                supportingService,
                nonSupportingService,
                promiscuousService));

        val context = OAuth20ConfigurationContext.builder()
            .servicesManager(serviceManager)
            .sessionStore(new JEESessionStore())
            .ticketRegistry(mockingTicketRegistry)
            .webApplicationServiceServiceFactory(new WebApplicationServiceFactory())
            .registeredServiceAccessStrategyEnforcer(new RegisteredServiceAccessStrategyAuditableEnforcer())
            .build();

        this.validator = new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(context);
    }

    @Test
    public void verifyOperation() {

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        val session = request.getSession(true);
        assertNotNull(session);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL);

        request.setParameter(OAuth20Constants.CODE, SUPPORTING_SERVICE_TICKET);
        assertTrue(this.validator.validate(new JEEContext(request, response)));

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(this.validator.validate(new JEEContext(request, response)));

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
        assertFalse(this.validator.validate(new JEEContext(request, response)));
        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());

        request.setParameter(OAuth20Constants.CODE, NON_SUPPORTING_SERVICE_TICKET);
        request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL2);
        profile.setId(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertFalse(this.validator.validate(new JEEContext(request, response)));

        request.setParameter(OAuth20Constants.CODE, PROMISCUOUS_SERVICE_TICKET);
        profile.setId(RequestValidatorTestUtils.PROMISCUOUS_CLIENT_ID);
        request.setParameter(OAuth20Constants.REDIRECT_URI, RegisteredServiceTestUtils.CONST_TEST_URL3);
        session.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        assertTrue(this.validator.validate(new JEEContext(request, response)));
    }
}
