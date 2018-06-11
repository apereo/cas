package org.apereo.cas.support.oauth.validator.token;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.*;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

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

@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasOAuthConfiguration.class,})

@RunWith(SpringRunner.class)
@Slf4j
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests {
    private OAuth20TokenRequestValidator validator;

    @Autowired
    @Qualifier("oauthCasAuthenticationBuilder")
    protected OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuthCodeFactory oAuthCodeFactory;

    private TicketRegistry ticketRegistry;
    private String oauthCodeId;

    @Before
    public void before() {
        final Service service = RegisteredServiceTestUtils.getService();

        final ServicesManager serviceManager = mock(ServicesManager.class);
        final OAuthRegisteredService registeredService = new OAuthRegisteredService();
        registeredService.setName("OAuth");
        registeredService.setClientId("client");
        registeredService.setClientSecret("secret");
        registeredService.setServiceId(service.getId());

        final Service oauthCasAuthenticationBuilderService = this.oauthCasAuthenticationBuilder.buildService(registeredService,  null, false);

        final OAuthCode oauthCode = oAuthCodeFactory.create(oauthCasAuthenticationBuilderService, RegisteredServiceTestUtils.getAuthentication(),
                new MockTicketGrantingTicket("casuser"),   new HashSet<>());

        this.ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(anyString(), any())).thenReturn(oauthCode);

        this.oauthCodeId = oauthCode.getId();

        when(serviceManager.getAllServices()).thenReturn(CollectionUtils.wrapList(registeredService));
        this.validator = new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(serviceManager,
            ticketRegistry, new RegisteredServiceAccessStrategyAuditableEnforcer());
    }

    @Test
    public void verifyOperation() {
        final Service service = RegisteredServiceTestUtils.getService();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.GRANT_TYPE, "unsupported");
        assertFalse(this.validator.validate(new J2EContext(request, response)));

        final CommonProfile profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("client");
        final HttpSession session = request.getSession(true);
        session.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getId());
        request.setParameter(OAuth20Constants.CODE, this.oauthCodeId);
        assertTrue(this.validator.validate(new J2EContext(request, response)));
    }
}
