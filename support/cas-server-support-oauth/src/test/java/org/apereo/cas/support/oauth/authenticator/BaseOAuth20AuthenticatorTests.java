package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasOAuth20AuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuth20ComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuth20Configuration;
import org.apereo.cas.config.CasOAuth20EndpointsConfiguration;
import org.apereo.cas.config.CasOAuth20ServicesConfiguration;
import org.apereo.cas.config.CasOAuth20TicketSerializationConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.web.config.CasCookieConfiguration;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;

import static org.mockito.Mockito.*;

/**
 * This is {@link BaseOAuth20AuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasOAuth20AuthenticationServiceSelectionStrategyConfiguration.class,
    CasOAuth20ComponentSerializationConfiguration.class,
    CasOAuth20Configuration.class,
    CasOAuth20EndpointsConfiguration.class,
    CasOAuth20ServicesConfiguration.class,
    CasOAuth20TicketSerializationConfiguration.class
})
@DirtiesContext
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseOAuth20AuthenticatorTests {
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    protected AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    protected ServiceFactory serviceFactory;

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    protected JwtBuilder accessTokenJwtBuilder;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    protected PrincipalResolver defaultPrincipalResolver;

    protected OAuthRegisteredService service;
    protected OAuthRegisteredService serviceJwtAccessToken;
    protected OAuthRegisteredService serviceWithoutSecret;
    protected OAuthRegisteredService serviceWithoutSecret2;

    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    @Autowired
    protected CasConfigurationProperties casProperties;

    protected static OAuth20AccessToken getAccessToken() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService();

        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getId()).thenReturn("ABCD");
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);

        return accessToken;
    }

    protected static OAuth20RefreshToken getRefreshToken(final OAuthRegisteredService service) {
        val tgt = new MockTicketGrantingTicket("casuser");

        val refreshToken = mock(OAuth20RefreshToken.class);
        when(refreshToken.getId()).thenReturn("ABCD");
        when(refreshToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(refreshToken.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(refreshToken.getClientId()).thenReturn(service.getClientId());
        when(refreshToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);

        return refreshToken;
    }

    @BeforeEach
    public void initialize() {
        service = new OAuthRegisteredService();
        service.setName("OAuth");
        service.setId(1);
        service.setServiceId("https://www.example.org");
        service.setClientSecret("secret");
        service.setClientId("client");

        serviceWithoutSecret = new OAuthRegisteredService();
        serviceWithoutSecret.setName("OAuth2");
        serviceWithoutSecret.setId(2);
        serviceWithoutSecret.setServiceId("https://www.example2.org");
        serviceWithoutSecret.setClientId("clientWithoutSecret");

        serviceWithoutSecret2 = new OAuthRegisteredService();
        serviceWithoutSecret2.setName("OAuth3");
        serviceWithoutSecret2.setId(3);
        serviceWithoutSecret2.setServiceId("https://www.example3org");
        serviceWithoutSecret2.setClientId("clientWithoutSecret2");

        serviceJwtAccessToken = new OAuthRegisteredService();
        serviceJwtAccessToken.setName("The registered service name");
        serviceJwtAccessToken.setServiceId("https://oauth.jwt.service");
        serviceJwtAccessToken.setClientId("clientid");
        serviceJwtAccessToken.setClientSecret("clientsecret");
        serviceJwtAccessToken.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        serviceJwtAccessToken.setJwtAccessToken(true);

        servicesManager.save(service, serviceWithoutSecret, serviceWithoutSecret2, serviceJwtAccessToken);
    }
}
