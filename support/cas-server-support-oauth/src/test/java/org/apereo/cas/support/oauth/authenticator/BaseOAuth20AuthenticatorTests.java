package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseOAuth20AuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseOAuth20AuthenticatorTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableRetry
@ExtendWith(CasTestExtension.class)
public abstract class BaseOAuth20AuthenticatorTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    protected CentralAuthenticationService centralAuthenticationService;
    
    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    protected AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
    protected OAuth20RequestParameterResolver oauthRequestParameterResolver;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> serviceFactory;

    @Autowired
    @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
    protected JwtBuilder accessTokenJwtBuilder;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    protected PrincipalResolver defaultPrincipalResolver;

    @Autowired
    @Qualifier("oauthClientAuthenticator")
    protected Authenticator oauthClientAuthenticator;

    @Autowired
    @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
    protected OAuth20ConfigurationContext configurationContext;

    @Autowired
    @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
    protected OAuth20ClientSecretValidator oauth20ClientSecretValidator;

    @Autowired
    @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
    protected AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    protected OAuthRegisteredService service;

    protected OAuthRegisteredService serviceJwtAccessToken;

    protected OAuthRegisteredService serviceWithoutSecret;

    protected OAuthRegisteredService serviceWithoutSecret2;

    protected OAuthRegisteredService serviceWithAttributesMapping;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @BeforeEach
    void initialize() {
        service = new OAuthRegisteredService();
        service.setName("OAuth");
        service.setId(RandomUtils.nextLong());
        service.setServiceId("https://www.example.org");
        service.setClientSecret("secret");
        service.setClientId("client");

        serviceWithoutSecret = new OAuthRegisteredService();
        serviceWithoutSecret.setName("OAuth2");
        serviceWithoutSecret.setId(RandomUtils.nextLong());
        serviceWithoutSecret.setServiceId("https://www.example2.org");
        serviceWithoutSecret.setClientId("clientWithoutSecret");

        serviceWithoutSecret2 = new OAuthRegisteredService();
        serviceWithoutSecret2.setName("OAuth3");
        serviceWithoutSecret2.setId(RandomUtils.nextLong());
        serviceWithoutSecret2.setServiceId("https://www.example3org");
        serviceWithoutSecret2.setClientId("clientWithoutSecret2");

        serviceJwtAccessToken = new OAuthRegisteredService();
        serviceJwtAccessToken.setName("The registered service name");
        serviceJwtAccessToken.setServiceId("https://oauth.jwt.service");
        serviceJwtAccessToken.setClientId("clientid");
        serviceJwtAccessToken.setId(RandomUtils.nextLong());
        serviceJwtAccessToken.setClientSecret("clientsecret");
        serviceJwtAccessToken.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        serviceJwtAccessToken.setJwtAccessToken(true);

        serviceWithAttributesMapping = new OAuthRegisteredService();
        serviceWithAttributesMapping.setName("OAuth5");
        serviceWithAttributesMapping.setId(RandomUtils.nextLong());
        serviceWithAttributesMapping.setServiceId("https://www.example5.org");
        serviceWithAttributesMapping.setClientSecret("secret");
        serviceWithAttributesMapping.setClientId("serviceWithAttributesMapping");

        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode("LOWER");
        serviceWithAttributesMapping.setUsernameAttributeProvider(provider);
        serviceWithAttributesMapping.setAttributeReleasePolicy(
            new ReturnAllowedAttributeReleasePolicy(List.of("eduPersonAffiliation")));

        servicesManager.save(service, serviceWithoutSecret, serviceWithoutSecret2, serviceJwtAccessToken, serviceWithAttributesMapping);
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasOAuth20AutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(CasAuthenticationEventExecutionPlanTestConfiguration.class)
    public static class SharedTestConfiguration {
    }

    protected static OAuth20AccessToken getAccessToken() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getId()).thenReturn(UUID.randomUUID().toString());
        when(accessToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getClientId()).thenReturn("client");
        when(accessToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);

        return accessToken;
    }

    protected static OAuth20Code getCode() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService();
        val oauthCode = mock(OAuth20Code.class);
        when(oauthCode.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(oauthCode.getId()).thenReturn(UUID.randomUUID().toString());
        when(oauthCode.getTicketGrantingTicket()).thenReturn(tgt);
        when(oauthCode.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(oauthCode.getService()).thenReturn(service);
        when(oauthCode.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        return oauthCode;
    }

    protected static OAuth20RefreshToken getRefreshToken(final OAuthRegisteredService service) {
        val tgt = new MockTicketGrantingTicket("casuser");

        val refreshToken = mock(OAuth20RefreshToken.class);
        when(refreshToken.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(refreshToken.getId()).thenReturn("ABCD");
        when(refreshToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(refreshToken.getAuthentication()).thenReturn(tgt.getAuthentication());
        when(refreshToken.getClientId()).thenReturn(service.getClientId());
        when(refreshToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);

        return refreshToken;
    }
}
