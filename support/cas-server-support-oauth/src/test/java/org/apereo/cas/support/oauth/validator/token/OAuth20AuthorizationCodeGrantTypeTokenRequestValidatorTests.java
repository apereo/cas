package org.apereo.cas.support.oauth.validator.token;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
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
import java.util.Set;

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
        CasOAuthConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreConfiguration.class,
        })
@RunWith(SpringRunner.class)
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidatorTests {
    private OAuth20TokenRequestValidator validator;

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    private OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder;


    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuthCodeFactory oAuthCodeFactory;


    @Autowired
    @Qualifier("oauthAuthorizationCodeResponseBuilder")
    public OAuth20AuthorizationResponseBuilder oAuth20AuthorizationCodeAuthorizationResponseBuilder;


    private TicketRegistry ticketRegistry;



    @Before
    public void before() {
        final Service service = RegisteredServiceTestUtils.getService();

        final ServicesManager serviceManager = mock(ServicesManager.class);
        final OAuthRegisteredService registeredService = new OAuthRegisteredService();
        registeredService.setName("OAuth");
        registeredService.setClientId("client");
        registeredService.setClientSecret("secret");
        registeredService.setServiceId(service.getId());


        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final Service oauthCasAuthenticationBuilderService = this.oauthCasAuthenticationBuilder.buildService(registeredService,  new J2EContext(request, response), false);


        J2EContext context= new J2EContext(request, response);

        final String grantType = StringUtils.defaultIfEmpty(context.getRequestParameter(OAuth20Constants.GRANT_TYPE),
                OAuth20GrantTypes.AUTHORIZATION_CODE.getType()).toUpperCase();
        final Set<String> scopes = OAuth20Utils.parseRequestScopes(context);

        AuthenticationResult authenticationResult = new DefaultAuthenticationResult(RegisteredServiceTestUtils.getAuthentication(), oauthCasAuthenticationBuilderService) ;

        final AccessTokenRequestDataHolder holder = new AccessTokenRequestDataHolder(oauthCasAuthenticationBuilderService, RegisteredServiceTestUtils.getAuthentication(),
                registeredService, centralAuthenticationService.createTicketGrantingTicket(authenticationResult), OAuth20GrantTypes.valueOf(grantType), scopes);
        final OAuthCode oauthCode = oAuthCodeFactory.create(holder.getService(), holder.getAuthentication(),
                holder.getTicketGrantingTicket(), holder.getScopes());


        this.ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(anyString(), any())).thenReturn(oauthCode);

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
        request.setParameter(OAuth20Constants.CODE, "OC-12345678");
        assertTrue(this.validator.validate(new J2EContext(request, response)));
    }
}
