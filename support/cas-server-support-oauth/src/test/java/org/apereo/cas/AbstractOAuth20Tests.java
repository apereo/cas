package org.apereo.cas;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuth20AuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuth20ComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuth20Configuration;
import org.apereo.cas.config.CasOAuth20EndpointsConfiguration;
import org.apereo.cas.config.CasOAuth20ThrottleConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.CasOAuth20TestAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20DeviceUserCodeApprovalEndpointController;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.web.config.CasCookieConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractOAuth20Tests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasOAuth20TestAuthenticationEventExecutionPlanConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreConfiguration.class,
    CasCookieConfiguration.class,
    CasThrottlingConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketComponentSerializationConfiguration.class,
    CasCoreUtilSerializationConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    AbstractOAuth20Tests.OAuth20TestConfiguration.class,
    CasThymeleafConfiguration.class,
    CasThemesConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasOAuth20AuthenticationServiceSelectionStrategyConfiguration.class,
    CasOAuth20ComponentSerializationConfiguration.class,
    CasOAuth20Configuration.class,
    CasOAuth20EndpointsConfiguration.class,
    CasOAuth20ThrottleConfiguration.class
})
@DirtiesContext
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Tag("OAuth")
@Slf4j
public abstract class AbstractOAuth20Tests {

    public static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);

    public static final String CONTEXT = OAuth20Constants.BASE_OAUTH20_URL + '/';

    public static final String CLIENT_ID = "1";

    public static final String CLIENT_SECRET = "secret";

    public static final String WRONG_CLIENT_SECRET = "wrongSecret";

    public static final String REDIRECT_URI = "http://someurl";

    public static final String OTHER_REDIRECT_URI = "http://someotherurl";

    public static final String ID = "casuser";

    public static final String NAME = "attributeName";

    public static final String ATTRIBUTES_PARAM = "attributes";

    public static final String NAME2 = "attributeName2";

    public static final String VALUE = "attributeValue";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String GOOD_USERNAME = "test";

    public static final String GOOD_PASSWORD = "test";

    public static final String CODE_CHALLENGE = "myclientcode";

    public static final String CODE_CHALLENGE_METHOD_PLAIN = "plain";

    public static final String FIRST_NAME_ATTRIBUTE = "firstName";

    public static final String FIRST_NAME = "jerome";

    public static final String LAST_NAME_ATTRIBUTE = "lastName";

    public static final String LAST_NAME = "LELEU";

    public static final String CAS_SERVER = "casserver";

    public static final String CAS_SCHEME = "https";

    public static final int CAS_PORT = 443;

    public static final int DELTA = 2;

    public static final int TIMEOUT = 7200;

    @Autowired
    @Qualifier("accessTokenController")
    protected OAuth20AccessTokenEndpointController accessTokenController;

    @Autowired
    @Qualifier("accessTokenResponseGenerator")
    protected OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    protected JwtBuilder accessTokenJwtBuilder;

    @Autowired
    @Qualifier("deviceUserCodeApprovalEndpointController")
    protected OAuth20DeviceUserCodeApprovalEndpointController deviceController;

    @Autowired
    @Qualifier("oauthResourceOwnerCredentialsResponseBuilder")
    protected OAuth20AuthorizationResponseBuilder oauthResourceOwnerCredentialsResponseBuilder;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    protected SecurityInterceptor requiresAuthenticationInterceptor;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory oAuthCodeFactory;

    @Autowired
    @Qualifier("defaultDeviceTokenFactory")
    protected OAuth20DeviceTokenFactory defaultDeviceTokenFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    protected OAuth20RefreshTokenFactory oAuthRefreshTokenFactory;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory defaultOAuthCodeFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("oauthAccessTokenJwtCipherExecutor")
    protected CipherExecutor oauthAccessTokenJwtCipherExecutor;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    protected OAuth20AccessTokenFactory defaultAccessTokenFactory;

    @Autowired
    @Qualifier("oauthTokenGenerator")
    protected OAuth20TokenGenerator oauthTokenGenerator;

    @Autowired
    protected CasConfigurationProperties casProperties;

    public static ExpirationPolicyBuilder alwaysExpiresExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder() {
            private static final long serialVersionUID = -9043565995104313970L;

            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return new AlwaysExpiresExpirationPolicy();
            }

            @Override
            public Class<Ticket> getTicketType() {
                return null;
            }
        };
    }

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

    protected static OAuthRegisteredService getRegisteredService(final String clientId,
                                                                 final String secret) {
        return getRegisteredService("https://oauth.example.org", clientId, secret, Set.of());
    }

    protected static OAuthRegisteredService getRegisteredService(final String serviceId,
                                                                 final String clientId,
                                                                 final String secret) {
        return getRegisteredService(serviceId, clientId, secret, Set.of());
    }

    protected static OAuthRegisteredService getRegisteredService(final String serviceId,
                                                                 final String secret,
                                                                 final Set<OAuth20GrantTypes> grantTypes) {
        return getRegisteredService(serviceId, CLIENT_ID, secret, grantTypes);
    }

    protected static OAuthRegisteredService getRegisteredService(final String serviceId,
                                                                 final String clientId,
                                                                 final String secret,
                                                                 final Set<OAuth20GrantTypes> grantTypes) {
        val service = new OAuthRegisteredService();
        service.setName("The registered service name");
        service.setServiceId(serviceId);
        service.setClientId(clientId);
        service.setClientSecret(secret);
        service.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        service.setSupportedGrantTypes(
            grantTypes.stream().map(OAuth20GrantTypes::getType).collect(Collectors.toCollection(HashSet::new)));
        return service;
    }

    protected static Principal createPrincipal() {
        val map = new HashMap<String, List<Object>>();
        map.put(NAME, List.of(VALUE));
        val list = List.of(VALUE, VALUE);
        map.put(NAME2, (List) list);

        return CoreAuthenticationTestUtils.getPrincipal(ID, map);
    }

    protected static Authentication getAuthentication(final Principal principal) {
        val metadata = new BasicCredentialMetaData(
            new BasicIdentifiableCredential(principal.getId()));
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult(principal.getClass().getCanonicalName(),
            metadata, principal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principal)
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addCredential(metadata)
            .addSuccess(principal.getClass().getCanonicalName(), handlerResult)
            .build();
    }

    protected OAuthRegisteredService addRegisteredService(final Set<OAuth20GrantTypes> grantTypes) {
        return addRegisteredService(false, grantTypes);
    }

    protected OAuthRegisteredService addRegisteredService(final boolean generateRefreshToken,
                                                          final Set<OAuth20GrantTypes> grantTypes) {
        return addRegisteredService(generateRefreshToken, grantTypes, CLIENT_SECRET);
    }

    protected OAuthRegisteredService addRegisteredService(final boolean generateRefreshToken,
                                                          final Set<OAuth20GrantTypes> grantTypes, final String clientSecret) {
        val registeredService = getRegisteredService(REDIRECT_URI, clientSecret, grantTypes);
        registeredService.setGenerateRefreshToken(generateRefreshToken);
        servicesManager.save(registeredService);
        return registeredService;
    }

    protected OAuthRegisteredService addRegisteredService(final Set<OAuth20GrantTypes> grantTypes, final String clientSecret) {
        return addRegisteredService(false, grantTypes, clientSecret);
    }

    protected OAuthRegisteredService addRegisteredService() {
        return addRegisteredService(false, EnumSet.noneOf(OAuth20GrantTypes.class));
    }

    protected void clearAllServices() {
        servicesManager.deleteAll();
        servicesManager.load();
    }

    @SneakyThrows
    protected Pair<String, String> assertClientOK(final OAuthRegisteredService service,
                                                  final boolean refreshToken) {
        return assertClientOK(service, refreshToken, null);
    }

    @SneakyThrows
    protected Pair<String, String> assertClientOK(final OAuthRegisteredService service,
                                                  final boolean refreshToken,
                                                  final String scopes) {

        val principal = createPrincipal();
        val code = addCode(principal, service);
        LOGGER.debug("Added code [{}] for principal [{}]", code, principal);
        
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val auth = CLIENT_ID + ':' + CLIENT_SECRET;
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        val header = HttpConstants.BASIC_HEADER_PREFIX + value;
        mockRequest.addHeader(HttpConstants.AUTHORIZATION_HEADER, header);
        LOGGER.debug("Created header [{}] for client id [{}]", header, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);

        if (StringUtils.isNotBlank(scopes)) {
            mockRequest.setParameter(OAuth20Constants.SCOPE, scopes);
        }

        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        val mockResponse = new MockHttpServletResponse();

        LOGGER.debug("Invoking authentication interceptor...");
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);

        LOGGER.debug("Submitting access token request...");
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertNull(this.ticketRegistry.getTicket(code.getId()));
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());

        var refreshTokenId = StringUtils.EMPTY;
        val model = mv.getModel();
        assertTrue(model.containsKey(OAuth20Constants.ACCESS_TOKEN));

        if (refreshToken) {
            assertTrue(model.containsKey(OAuth20Constants.REFRESH_TOKEN));
            refreshTokenId = model.get(OAuth20Constants.REFRESH_TOKEN).toString();
        }
        assertTrue(model.containsKey(OAuth20Constants.EXPIRES_IN));
        val accessTokenId = extractAccessTokenFrom(model.get(OAuth20Constants.ACCESS_TOKEN).toString());

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(model.get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);

        return Pair.of(accessTokenId, refreshTokenId);
    }

    protected OAuth20Code addCode(final Principal principal, final OAuthRegisteredService registeredService) {
        return addCodeWithChallenge(principal, registeredService, null, null);
    }

    protected OAuth20Code addCodeWithChallenge(final Principal principal, final OAuthRegisteredService registeredService,
                                               final String codeChallenge, final String codeChallengeMethod) {
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getClientId());
        val code = oAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(),
            codeChallenge, codeChallengeMethod, CLIENT_ID, new HashMap<>());
        this.ticketRegistry.addTicket(code);
        return code;
    }

    /**
     * Extract access token from token.
     *
     * @param token the token
     * @return the string
     */
    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .decode(token);
    }

    protected OAuth20RefreshToken addRefreshToken(final Principal principal, final OAuthRegisteredService registeredService) {
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val refreshToken = oAuthRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), registeredService.getClientId(), StringUtils.EMPTY, new HashMap<>());
        this.ticketRegistry.addTicket(refreshToken);
        return refreshToken;
    }

    protected OAuth20RefreshToken addRefreshToken(final Principal principal, final OAuthRegisteredService registeredService, final OAuth20AccessToken accessToken) {
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val refreshToken = oAuthRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), registeredService.getClientId(), accessToken.getId(), new HashMap<>());
        this.ticketRegistry.addTicket(refreshToken);
        return refreshToken;
    }

    protected OAuth20AccessToken addAccessToken(final Principal principal, final OAuthRegisteredService registeredService) {
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val accessToken = defaultAccessTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), registeredService.getClientId(), new HashMap<>());
        this.ticketRegistry.addTicket(accessToken);
        return accessToken;
    }

    @SneakyThrows
    protected Pair<OAuth20AccessToken, OAuth20RefreshToken> assertRefreshTokenOk(final OAuthRegisteredService service) {
        val principal = createPrincipal();
        val refreshToken = addRefreshToken(principal, service);
        return assertRefreshTokenOk(service, refreshToken, principal);
    }

    protected Pair<OAuth20AccessToken, OAuth20RefreshToken> assertRefreshTokenOk(final OAuthRegisteredService service,
                                                                                 final OAuth20RefreshToken refreshToken, final Principal principal) throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = accessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());

        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        if (service.isGenerateRefreshToken()) {
            assertTrue(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
            if (service.isRenewRefreshToken()) {
                assertNull(this.ticketRegistry.getTicket(refreshToken.getId()));
            }
        }
        val newRefreshToken = service.isRenewRefreshToken()
            ? this.ticketRegistry.getTicket(mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString(), OAuth20RefreshToken.class)
            : refreshToken;

        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);

        return Pair.of(accessToken, newRefreshToken);
    }

    protected ModelAndView generateAccessTokenResponseAndGetModelAndView(final OAuthRegisteredService registeredService) {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        val mockResponse = new MockHttpServletResponse();

        val service = RegisteredServiceTestUtils.getService("example");
        val holder = AccessTokenRequestDataHolder.builder()
            .clientId(registeredService.getClientId())
            .service(service)
            .authentication(RegisteredServiceTestUtils.getAuthentication("casuser"))
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();

        val generatedToken = oauthTokenGenerator.generate(holder);
        val builder = OAuth20AccessTokenResponseResult.builder();
        val result = builder
            .registeredService(registeredService)
            .responseType(OAuth20ResponseTypes.CODE)
            .service(service)
            .generatedToken(generatedToken)
            .build();
        return accessTokenResponseGenerator.generate(mockRequest, mockResponse, result);
    }

    @TestConfiguration("OAuth20TestConfiguration")
    @Lazy(false)
    public static class OAuth20TestConfiguration implements ComponentSerializationPlanConfigurer, InitializingBean {
        @Autowired
        protected ApplicationContext applicationContext;

        @Override
        public void afterPropertiesSet() {
            init();
        }

        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }

        @Bean
        public List inMemoryRegisteredServices() {
            val svc1 = (OAuthRegisteredService)
                RegisteredServiceTestUtils.getRegisteredService("^(https?|imaps?)://.*", OAuthRegisteredService.class);
            svc1.setClientId(UUID.randomUUID().toString());
            svc1.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

            val svc2 = (OAuthRegisteredService)
                RegisteredServiceTestUtils.getRegisteredService("https://example.org/jwt-access-token", OAuthRegisteredService.class);
            svc2.setClientId(CLIENT_ID);
            svc2.setJwtAccessToken(true);

            return CollectionUtils.wrapList(svc1, svc2);
        }

        @Override
        public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
            plan.registerSerializableClass(MockTicketGrantingTicket.class);
            plan.registerSerializableClass(MockServiceTicket.class);
        }
    }
}
