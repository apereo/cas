package org.apereo.cas.oidc;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuth20AuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuth20Configuration;
import org.apereo.cas.config.CasOAuth20EndpointsConfiguration;
import org.apereo.cas.config.CasOAuth20ThrottleConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.config.OidcConfiguration;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponseTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.webflow.execution.Action;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractOidcTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    OidcConfiguration.class,
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCookieConfiguration.class,
    CasThemesConfiguration.class,
    CasThymeleafConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasOAuth20Configuration.class,
    CasOAuth20EndpointsConfiguration.class,
    OidcClientRegistrationResponseTests.class,
    CasThrottlingConfiguration.class,
    CasOAuth20ThrottleConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasOAuth20AuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class
},
    properties = {
        "cas.authn.oidc.issuer=https://sso.example.org/cas/oidc",
        "cas.authn.oidc.jwks.jwksFile=classpath:keystore.jwks"
    })
@DirtiesContext
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("OIDC")
public abstract class AbstractOidcTests {

    protected static final String TGT_ID = "TGT-0";

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("oauthInterceptor")
    protected HandlerInterceptorAdapter oauthInterceptor;

    @Autowired
    @Qualifier("oidcWebFingerDiscoveryService")
    protected OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService;

    @Autowired
    @Qualifier("oidcImplicitIdTokenAndTokenCallbackUrlBuilder")
    protected OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenAndTokenCallbackUrlBuilder;

    @Autowired
    @Qualifier("oauthRegisteredServiceJwtAccessTokenCipherExecutor")
    protected RegisteredServiceCipherExecutor oauthRegisteredServiceJwtAccessTokenCipherExecutor;

    @Autowired
    @Qualifier("oidcUserProfileViewRenderer")
    protected OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer;

    @Autowired
    @Qualifier("defaultDeviceTokenFactory")
    protected OAuth20DeviceTokenFactory deviceTokenFactory;

    @Autowired
    @Qualifier("oidcUserProfileDataCreator")
    protected OAuth20UserProfileDataCreator oidcUserProfileDataCreator;

    @Autowired
    @Qualifier("oauthCasClientRedirectActionBuilder")
    protected OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder;

    @Autowired
    @Qualifier("profileScopeToAttributesFilter")
    protected OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    @Autowired
    @Qualifier("oidcUserProfileSigningAndEncryptionService")
    protected OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService;

    @Autowired
    @Qualifier("oidcServiceRegistryListener")
    protected ServiceRegistryListener oidcServiceRegistryListener;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory defaultOAuthCodeFactory;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("callbackAuthorizeViewResolver")
    protected OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("oidcDefaultJsonWebKeystoreCache")
    protected LoadingCache<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCache;

    @Autowired
    @Qualifier("oidcTokenSigningAndEncryptionService")
    protected OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService;

    @Autowired
    @Qualifier("oidcServiceJsonWebKeystoreCache")
    protected LoadingCache<OAuthRegisteredService, Optional<PublicJsonWebKey>> oidcServiceJsonWebKeystoreCache;

    @Autowired
    @Qualifier("oidcJsonWebKeystoreGeneratorService")
    protected OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    protected AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("oidcRegisteredServiceUIAction")
    protected Action oidcRegisteredServiceUIAction;

    @Autowired
    @Qualifier("oidcServerDiscoverySettingsFactory")
    protected OidcServerDiscoverySettings oidcServerDiscoverySettings;

    @Autowired
    @Qualifier("oidcAccessTokenResponseGenerator")
    protected OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator;

    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("oidcIdTokenGenerator")
    protected IdTokenGeneratorService oidcIdTokenGenerator;

    @Autowired
    @Qualifier("consentApprovalViewResolver")
    protected ConsentApprovalViewResolver consentApprovalViewResolver;

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    protected JwtBuilder accessTokenJwtBuilder;

    protected static OidcRegisteredService getOidcRegisteredService() {
        return getOidcRegisteredService(true, true);
    }

    protected static OidcRegisteredService getOidcRegisteredService(final boolean sign,
                                                                    final boolean encrypt) {
        return getOidcRegisteredService("clientid", "https://oauth\\.example\\.org.*", sign, encrypt);
    }

    protected static OidcRegisteredService getOidcRegisteredService(final String clientid) {
        return getOidcRegisteredService(clientid, "https://oauth\\.example\\.org.*", true, true);
    }

    protected static OidcRegisteredService getOidcRegisteredService(final String clientId,
                                                                    final String redirectUri,
                                                                    final boolean sign,
                                                                    final boolean encrypt) {
        val svc = new OidcRegisteredService();
        svc.setClientId(clientId);
        svc.setName("oauth");
        svc.setDescription("description");
        svc.setClientSecret("secret");
        svc.setServiceId(redirectUri);
        svc.setSignIdToken(sign);
        svc.setEncryptIdToken(encrypt);
        svc.setIdTokenEncryptionAlg(KeyManagementAlgorithmIdentifiers.RSA_OAEP_256);
        svc.setIdTokenEncryptionEncoding(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        svc.setInformationUrl("info");
        svc.setPrivacyUrl("privacy");
        svc.setJwks("classpath:keystore.jwks");
        svc.setLogoutUrl("https://oauth.example.org/logout,https://logout");
        svc.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        svc.setScopes(CollectionUtils.wrapSet(OidcConstants.StandardScopes.EMAIL.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope()));
        return svc;
    }

    protected static OAuthRegisteredService getOAuthRegisteredService(final String clientId,
                                                                      final String redirectUri) {
        val svc = new OAuthRegisteredService();
        svc.setClientId(clientId);
        svc.setName("oauth");
        svc.setDescription("description");
        svc.setClientSecret("secret");
        svc.setServiceId(redirectUri);
        svc.setInformationUrl("info");
        svc.setPrivacyUrl("privacy");
        return svc;
    }

    protected JwtClaims getClaims() {
        val clientId = getOidcRegisteredService().getClientId();
        return getClaims("casuser", casProperties.getAuthn().getOidc().getIssuer(), clientId, clientId);
    }

    protected static JwtClaims getClaims(final String subject, final String issuer,
                                         final String clientId, final String audience) {
        val claims = new JwtClaims();
        claims.setJwtId(RandomUtils.randomAlphanumeric(16));
        claims.setIssuer(issuer);
        claims.setAudience(audience);

        val expirationDate = NumericDate.now();
        expirationDate.addSeconds(120);
        claims.setExpirationTime(expirationDate);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(1);
        claims.setSubject(subject);
        claims.setStringClaim(OAuth20Constants.CLIENT_ID, clientId);
        return claims;
    }

    protected static OAuth20AccessToken getAccessToken() {
        return getAccessToken(StringUtils.EMPTY, "clientId");
    }

    protected static OAuth20AccessToken getAccessToken(final String clientId) {
        return getAccessToken(StringUtils.EMPTY, clientId);
    }

    protected static OAuth20AccessToken getAccessToken(final String idToken, final String clientId) {
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("email", List.of("casuser@example.org")));
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication(principal));
        when(accessToken.getService()).thenReturn(RegisteredServiceTestUtils.getService("https://oauth.example.org"));
        when(accessToken.getId()).thenReturn("AT-123456");
        when(accessToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(accessToken.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(accessToken.getClientId()).thenReturn(clientId);
        when(accessToken.getCreationTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        when(accessToken.getScopes()).thenReturn(Set.of(OidcConstants.StandardScopes.EMAIL.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));
        when(accessToken.getIdToken()).thenReturn(idToken);
        return accessToken;
    }

    protected static OAuth20RefreshToken getRefreshToken() {
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("email", List.of("casuser@example.org")));
        val token = mock(OAuth20RefreshToken.class);
        when(token.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication(principal));
        when(token.getService()).thenReturn(RegisteredServiceTestUtils.getService("https://oauth.example.org"));
        when(token.getId()).thenReturn("RT-123456");
        when(token.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(token.getScopes()).thenReturn(Set.of(OidcConstants.StandardScopes.EMAIL.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));
        when(token.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        return token;
    }


    @BeforeEach
    public void initialize() {
        servicesManager.save(getOidcRegisteredService());
    }
}
