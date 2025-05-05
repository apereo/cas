package org.apereo.cas.oidc;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
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
import org.apereo.cas.config.CasOidcAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThrottlingAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasWebAppAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.claims.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcCibaRequestFactory;
import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.idtoken.IdTokenGeneratorService;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.CasEventListener;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.webflow.execution.Action;
import java.io.Serializable;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractOidcTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = AbstractOidcTests.SharedTestConfiguration.class,
    properties = {
        "spring.threads.virtual.enabled=true",
        "cas.audit.slf4j.use-single-line=true",

        "cas.server.name=https://sso.example.org/",
        "cas.server.prefix=https://sso.example.org/cas",

        "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc",
        "cas.authn.oidc.jwks.file-system.jwks-file=classpath:keystore.jwks"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class AbstractOidcTests {
    protected static final String TGT_ID = "TGT-0";

    @Autowired
    @Qualifier("oauthTokenGenerator")
    protected OAuth20TokenGenerator oauthTokenGenerator;

    @Autowired
    @Qualifier(OAuth20ResponseModeFactory.BEAN_NAME)
    protected OAuth20ResponseModeFactory oauthResponseModeFactory;

    @Autowired
    @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
    protected OAuth20RequestParameterResolver oauthRequestParameterResolver;

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    protected CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("oidcMultifactorAuthenticationTrigger")
    protected MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger;

    @Autowired
    @Qualifier(OidcIssuerService.BEAN_NAME)
    protected OidcIssuerService oidcIssuerService;

    @Autowired
    @Qualifier("oidcJsonWebKeystoreRotationService")
    protected OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService;

    @Autowired
    @Qualifier("singleLogoutServiceLogoutUrlBuilder")
    protected SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    @Autowired
    protected ConfigurableWebApplicationContext applicationContext;

    @Autowired
    @Qualifier("oauthDistributedSessionStore")
    protected SessionStore oauthDistributedSessionStore;

    @Autowired
    @Qualifier("oauthInterceptor")
    protected HandlerInterceptor oauthInterceptor;

    @Autowired
    @Qualifier("oidcWebFingerDiscoveryService")
    protected OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService;

    @Autowired
    @Qualifier("oidcImplicitIdTokenAndTokenCallbackUrlBuilder")
    protected OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenAndTokenCallbackUrlBuilder;

    @Autowired
    @Qualifier("oidcImplicitIdTokenCallbackUrlBuilder")
    protected OAuth20AuthorizationResponseBuilder oidcImplicitIdTokenCallbackUrlBuilder;

    @Autowired
    @Qualifier("oidcRegisteredServiceJwtAccessTokenCipherExecutor")
    protected RegisteredServiceCipherExecutor oidcRegisteredServiceJwtAccessTokenCipherExecutor;

    @Autowired
    @Qualifier("oidcAccessTokenJwtCipherExecutor")
    protected CipherExecutor<Serializable, String> oidcAccessTokenJwtCipherExecutor;

    @Autowired
    @Qualifier("oidcResponseModeJwtCipherExecutor")
    protected CipherExecutor<Serializable, String> oidcResponseModeJwtCipherExecutor;

    @Autowired
    @Qualifier("oidcUserProfileViewRenderer")
    protected OAuth20UserProfileViewRenderer oidcUserProfileViewRenderer;

    @Autowired
    @Qualifier("defaultDeviceTokenFactory")
    protected OAuth20DeviceTokenFactory deviceTokenFactory;

    @Autowired
    @Qualifier("defaultDeviceUserCodeFactory")
    protected OAuth20DeviceUserCodeFactory deviceUserCodeFactory;

    @Autowired
    @Qualifier("oidcUserProfileDataCreator")
    protected OAuth20UserProfileDataCreator oidcUserProfileDataCreator;

    @Autowired
    @Qualifier("oauthCasClientRedirectActionBuilder")
    protected OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder;

    @Autowired
    @Qualifier(OAuth20ProfileScopeToAttributesFilter.BEAN_NAME)
    protected OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    @Autowired
    @Qualifier("oidcUserProfileSigningAndEncryptionService")
    protected OAuth20TokenSigningAndEncryptionService oidcUserProfileSigningAndEncryptionService;

    @Autowired
    @Qualifier("oidcServiceRegistryListener")
    protected ServiceRegistryListener oidcServiceRegistryListener;

    @Autowired
    @Qualifier("oidcJsonWebKeyStoreListener")
    protected CasEventListener oidcJsonWebKeyStoreListener;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory defaultOAuthCodeFactory;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("callbackAuthorizeViewResolver")
    protected OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(OidcConfigurationContext.BEAN_NAME)
    protected OidcConfigurationContext oidcConfigurationContext;

    @Autowired
    @Qualifier("oidcDefaultJsonWebKeystoreCache")
    protected LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache;

    @Autowired
    @Qualifier("oidcTokenSigningAndEncryptionService")
    protected OAuth20TokenSigningAndEncryptionService oidcTokenSigningAndEncryptionService;

    @Autowired
    @Qualifier("oidcServiceJsonWebKeystoreCache")
    protected LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcServiceJsonWebKeystoreCache;

    @Autowired
    @Qualifier("oidcJsonWebKeystoreGeneratorService")
    protected OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService;

    @Autowired
    @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
    protected AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_OIDC_REGISTERED_SERVICE_UI)
    protected Action oidcRegisteredServiceUIAction;

    @Autowired
    @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
    protected OidcServerDiscoverySettings oidcServerDiscoverySettings;

    @Autowired
    @Qualifier("oidcAccessTokenResponseGenerator")
    protected OAuth20AccessTokenResponseGenerator oidcAccessTokenResponseGenerator;

    @Autowired
    @Qualifier(OidcAttributeToScopeClaimMapper.DEFAULT_BEAN_NAME)
    protected OidcAttributeToScopeClaimMapper oidcAttributeToScopeClaimMapper;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    protected TicketFactory defaultTicketFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("oidcIdTokenGenerator")
    protected IdTokenGeneratorService oidcIdTokenGenerator;

    @Autowired
    @Qualifier("consentApprovalViewResolver")
    protected ConsentApprovalViewResolver consentApprovalViewResolver;

    @Autowired
    @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
    protected JwtBuilder oidcAccessTokenJwtBuilder;

    @Autowired
    @Qualifier("accessTokenExpirationPolicy")
    protected ExpirationPolicyBuilder accessTokenExpirationPolicy;

    @Autowired
    @Qualifier("webflowCipherExecutor")
    protected CipherExecutor webflowCipherExecutor;

    protected static OidcRegisteredService getOidcRegisteredService() {
        return getOidcRegisteredService(true, true);
    }

    protected static OidcRegisteredService getOidcRegisteredService(final boolean sign, final boolean encrypt) {
        return getOidcRegisteredService("clientid", "https://oauth\\.example\\.org.*", sign, encrypt);
    }

    protected static OidcRegisteredService getOidcRegisteredService(final String clientId, final String redirectUri) {
        return getOidcRegisteredService(clientId, redirectUri, true, true);
    }

    protected static OidcRegisteredService getOidcRegisteredService(final String clientId) {
        return getOidcRegisteredService(clientId, "https://oauth\\.example\\.org.*", true, true);
    }

    protected static OidcRegisteredService getOidcRegisteredService(final String clientId,
                                                                    final String redirectUri,
                                                                    final boolean sign,
                                                                    final boolean encrypt) {
        val svc = new OidcRegisteredService();
        svc.setClientId(clientId);
        svc.setName("oauth-%s".formatted(UUID.randomUUID().toString()));
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
        svc.setLogoutUrl("https://oauth.example.org/logout,https://logout,https://www.acme.com/.*");
        svc.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        svc.setScopes(CollectionUtils.wrapSet(
            OidcConstants.StandardScopes.OPENID.getScope(),
            OidcConstants.StandardScopes.EMAIL.getScope(),
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

    protected static OAuth20RefreshToken getRefreshToken() {
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("email", List.of("casuser@example.org")));
        val token = mock(OAuth20RefreshToken.class);
        when(token.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication(principal));
        val service = RegisteredServiceTestUtils.getService("https://oauth.example.org");
        when(token.getService()).thenReturn(service);
        when(token.getId()).thenReturn("RT-123456");
        when(token.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(token.getScopes()).thenReturn(Set.of(OidcConstants.StandardScopes.EMAIL.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));
        when(token.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        return token;
    }

    protected static MockHttpServletRequest getHttpRequestForEndpoint(final String endpoint) {
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("sso.example.org");
        request.setServerPort(443);
        request.setRequestURI("/cas/oidc/" + endpoint);
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        return request;
    }

    @BeforeEach
    protected void initialize() throws Throwable {
        servicesManager.save(getOidcRegisteredService());
        ticketRegistry.deleteAll();
    }

    protected JwtClaims getClaims() {
        return getClaims(getOidcRegisteredService().getClientId());
    }

    protected JwtClaims getClaims(final String clientId) {
        return getClaims("casuser", casProperties.getAuthn().getOidc().getCore().getIssuer(), clientId, clientId);
    }

    protected JwtClaims getClaims(final String subject, final String issuer,
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

    protected OAuth20AccessToken getAccessToken(final Principal principal) throws Throwable {
        return getAccessToken(principal, StringUtils.EMPTY, "clientid");
    }

    protected OAuth20AccessToken getAccessToken() throws Throwable {
        return getAccessToken(StringUtils.EMPTY, "clientid");
    }

    protected OAuth20AccessToken getAccessToken(final String clientId) throws Throwable {
        return getAccessToken(StringUtils.EMPTY, clientId);
    }

    protected OAuth20AccessToken getAccessToken(final String idToken, final String clientId) throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("email", List.of("casuser@example.org")));
        return getAccessToken(principal, idToken, clientId);
    }

    protected OAuth20AccessToken getAccessToken(final Principal principal, final String clientId) throws Throwable {
        return getAccessToken(principal, StringUtils.EMPTY, clientId);
    }

    protected OAuth20AccessToken getAccessToken(final Authentication authentication, final String clientId) throws Throwable {
        val at = getAccessToken(authentication.getPrincipal(), StringUtils.EMPTY, clientId);
        when(at.getAuthentication()).thenReturn(authentication);
        return at;
    }

    protected OAuth20AccessToken getAccessToken(final Principal principal, final String idToken,
                                                final String clientId) throws Throwable {
        val code = addCode(principal, getOidcRegisteredService(clientId));
        val accessToken = mock(OAuth20AccessToken.class);
        when(accessToken.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication(principal));
        val service = RegisteredServiceTestUtils.getService("https://oauth.example.org");
        when(accessToken.getService()).thenReturn(service);
        when(accessToken.getId()).thenReturn("AT-" + UUID.randomUUID());
        when(accessToken.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        when(accessToken.getTicketGrantingTicket()).thenReturn(new MockTicketGrantingTicket("casuser"));
        when(accessToken.getClientId()).thenReturn(clientId);
        when(accessToken.getCreationTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        when(accessToken.getScopes()).thenReturn(Set.of(OidcConstants.StandardScopes.EMAIL.getScope(),
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));
        when(accessToken.getToken()).thenReturn(code.getId());
        when(accessToken.getIdToken()).thenReturn(idToken);
        when(accessToken.getExpiresIn()).thenReturn(Duration.ofDays(365 * 5).toSeconds());
        when(accessToken.getGrantType()).thenReturn(OAuth20GrantTypes.AUTHORIZATION_CODE);
        when(accessToken.getPrefix()).thenReturn(OAuth20AccessToken.PREFIX);
        return accessToken;
    }

    protected String randomServiceUrl() {
        return "https://app.example.org/%s".formatted(RandomUtils.randomAlphabetic(8));
    }

    protected OAuth20Code addCode(final Principal principal,
                                  final OAuthRegisteredService registeredService) throws Throwable {
        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        val service = webApplicationServiceFactory.createService(registeredService.getClientId());
        val scopes = List.of(OidcConstants.StandardScopes.OPENID.getScope());
        val code = defaultOAuthCodeFactory.create(service, authentication,
            ticketGrantingTicket, scopes, registeredService.getClientId(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        return code;
    }

    protected OAuth20Code addCode(final TicketGrantingTicket ticketGrantingTicket,
                                  final OAuthRegisteredService registeredService) throws Throwable {
        val authentication = ticketGrantingTicket.getAuthentication();
        val service = webApplicationServiceFactory.createService(registeredService.getClientId());
        val scopes = List.of(OidcConstants.StandardScopes.OPENID.getScope());
        val code = defaultOAuthCodeFactory.create(service, authentication,
            ticketGrantingTicket, scopes, registeredService.getClientId(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        return code;
    }

    protected OidcCibaRequest newCibaRequest(final OidcRegisteredService registeredService,
                                             final Principal principal) throws Throwable {
        val cibaRequestContext = CibaRequestContext.builder()
            .clientNotificationToken(UUID.randomUUID().toString())
            .clientId(registeredService.getClientId())
            .scope(Set.of(OidcConstants.StandardScopes.OPENID.getScope()))
            .userCode(UUID.randomUUID().toString())
            .principal(principal)
            .build();
        val cibaFactory = (OidcCibaRequestFactory) defaultTicketFactory.get(OidcCibaRequest.class);
        val cibaRequestId = cibaFactory.create(cibaRequestContext);
        ticketRegistry.addTicket(cibaRequestId);
        return cibaRequestId;
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasThymeleafAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasThrottlingAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasOidcAutoConfiguration.class,
        CasOAuth20AutoConfiguration.class,
        CasWebAppAutoConfiguration.class
    })
    @Import({
        CasRegisteredServicesTestConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
