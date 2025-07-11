package org.apereo.cas;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
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
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThrottlingAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasWebAppAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.RegisteredServicesTemplatesManager;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.CasOAuth20AuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20DeviceUserCodeApprovalEndpointController;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpStatus;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractOAuth20Tests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = AbstractOAuth20Tests.SharedTestConfiguration.class,
    properties = {
        "cas.audit.engine.audit-format=JSON",
        "cas.audit.slf4j.use-single-line=true",

        "cas.authn.attribute-repository.stub.attributes.uid=cas",
        "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",

        "cas.authn.oauth.session-replication.cookie.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        "cas.authn.oauth.session-replication.cookie.crypto.encryption.key=3RXtt06xYUAli7uU-Z915ZGe0MRBFw3uDjWgOEf1GT8",
        "cas.authn.oauth.session-replication.cookie.crypto.signing.key=jIFR-fojN0vOIUcT0hDRXHLVp07CV-YeU8GnjICsXpu65lfkJbiKP028pT74Iurkor38xDGXNcXk_Y1V4rNDqw"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@AutoConfigureMockMvc
@Slf4j
public abstract class AbstractOAuth20Tests {

    public static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);

    public static final String CONTEXT = OAuth20Constants.BASE_OAUTH20_URL + '/';

    public static final String CLIENT_SECRET = "secret";

    public static final String WRONG_CLIENT_SECRET = "wrongSecret";

    public static final String REDIRECT_URI = "http://someurl";

    public static final String OTHER_REDIRECT_URI = "http://someotherurl";

    public static final String SERVICE_URL = "http://serviceurl";

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
    @Qualifier(RegisteredServicesTemplatesManager.BEAN_NAME)
    protected RegisteredServicesTemplatesManager registeredServicesTemplatesManager;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("oauthSecConfig")
    protected Config oauthSecConfig;

    @Autowired
    @Qualifier("oauthCasClient")
    protected Client oauthCasClient;

    @Autowired
    @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
    protected OAuth20ConfigurationContext configurationContext;

    @Autowired
    @Qualifier("oauthCasClientRedirectActionBuilder")
    protected OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> serviceFactory;

    @Autowired
    @Qualifier("oauthHandlerInterceptorAdapter")
    protected HandlerInterceptor oauthHandlerInterceptorAdapter;

    @Autowired
    @Qualifier(ServicesManagerConfigurationContext.BEAN_NAME)
    protected ServicesManagerConfigurationContext servicesManagerConfigurationContext;

    @Autowired
    @Qualifier("accessTokenController")
    protected OAuth20AccessTokenEndpointController accessTokenController;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    protected RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    @Autowired
    @Qualifier("oauthDistributedSessionStore")
    protected SessionStore oauthDistributedSessionStore;

    @Autowired
    @Qualifier("oauthAuthorizationCodeResponseBuilder")
    protected OAuth20AuthorizationResponseBuilder oauthAuthorizationCodeResponseBuilder;

    @Autowired
    @Qualifier("oauthTokenResponseBuilder")
    protected OAuth20AuthorizationResponseBuilder oauthTokenResponseBuilder;

    @Autowired
    @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
    protected OAuth20ClientSecretValidator oauth20ClientSecretValidator;

    @Autowired
    @Qualifier("accessTokenResponseGenerator")
    protected OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;

    @Autowired
    @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
    protected JwtBuilder accessTokenJwtBuilder;

    @Autowired
    @Qualifier("deviceUserCodeApprovalEndpointController")
    protected OAuth20DeviceUserCodeApprovalEndpointController deviceController;

    @Autowired
    @Qualifier("oauthResourceOwnerCredentialsResponseBuilder")
    protected OAuth20AuthorizationResponseBuilder oauthResourceOwnerCredentialsResponseBuilder;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    protected PrincipalResolver principalResolver;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    protected HandlerInterceptor requiresAuthenticationInterceptor;

    @Autowired
    protected ConfigurableWebApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory oAuthCodeFactory;

    @Autowired
    @Qualifier("defaultDeviceTokenFactory")
    protected OAuth20DeviceTokenFactory defaultDeviceTokenFactory;

    @Autowired
    @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
    protected OAuth20RequestParameterResolver oauthRequestParameterResolver;

    @Autowired
    @Qualifier("defaultDeviceUserCodeFactory")
    protected OAuth20DeviceUserCodeFactory defaultDeviceUserCodeFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    protected OAuth20RefreshTokenFactory defaultRefreshTokenFactory;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory defaultOAuthCodeFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
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
    @Qualifier("deviceTokenExpirationPolicy")
    protected ExpirationPolicyBuilder deviceTokenExpirationPolicy;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_DESCENDANT_TICKET_TRACKING)
    protected TicketTrackingPolicy descendantTicketsTrackingPolicy;

    @Autowired
    @Qualifier("accessTokenTokenExchangeGrantRequestExtractor")
    protected AccessTokenGrantRequestExtractor accessTokenTokenExchangeGrantRequestExtractor;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mockMvc")
    protected MockMvc mockMvc;

    public static ExpirationPolicyBuilder alwaysExpiresExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder() {
            @Serial
            private static final long serialVersionUID = -9043565995104313970L;

            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return AlwaysExpiresExpirationPolicy.INSTANCE;
            }
        };
    }

    protected static OAuth20RefreshToken getRefreshToken() {
        return OAuth20TestUtils.getRefreshToken(RegisteredServiceTestUtils.CONST_TEST_URL, UUID.randomUUID().toString());
    }

    protected static OAuth20AccessToken getAccessToken(final String id, final String serviceId, final String clientId) {
        return OAuth20TestUtils.getAccessToken(new MockTicketGrantingTicket("casuser"), id, serviceId, clientId);
    }

    protected static OAuth20AccessToken getAccessToken(final Authentication authentication, final String serviceId, final String clientId) {
        return OAuth20TestUtils.getAccessToken(new MockTicketGrantingTicket(authentication), UUID.randomUUID().toString(), serviceId, clientId);
    }


    protected static OAuth20AccessToken getAccessToken(final String serviceId, final String clientId) {
        return getAccessToken(UUID.randomUUID().toString(), serviceId, clientId);
    }

    protected static OAuth20AccessToken getAccessToken(final String serviceId) {
        return getAccessToken(serviceId, UUID.randomUUID().toString());
    }

    protected static OAuth20AccessToken getAccessToken() {
        return getAccessToken(RegisteredServiceTestUtils.CONST_TEST_URL, UUID.randomUUID().toString());
    }

    protected static OAuthRegisteredService getRegisteredService(final String clientId, final OAuth20GrantTypes... grantTypes) {
        return getRegisteredService("https://oauth-%s.example.org".formatted(RandomUtils.randomAlphabetic(6)),
            clientId, UUID.randomUUID().toString(), Set.of(grantTypes));
    }

    protected static OAuthRegisteredService getRegisteredService(final OAuth20GrantTypes... grantTypes) {
        return getRegisteredService(UUID.randomUUID().toString(), grantTypes);
    }

    protected static OAuthRegisteredService getRegisteredService(final String clientId, final String secret) {
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
        return getRegisteredService(serviceId, UUID.randomUUID().toString(), secret, grantTypes);
    }

    protected static OAuthRegisteredService getRegisteredService(final String serviceId,
                                                                 final String clientId,
                                                                 final String secret,
                                                                 final Set<OAuth20GrantTypes> grantTypes) {
        val service = new OAuthRegisteredService();
        service.setName("RegisteredService-" + RandomUtils.randomAlphabetic(6));
        service.setServiceId(serviceId);
        service.setClientId(clientId);
        service.setClientSecret(secret);
        service.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        val types = grantTypes.stream().map(OAuth20GrantTypes::getType).collect(Collectors.toSet());
        service.setSupportedGrantTypes(types);
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
        val metadata = new BasicIdentifiableCredential(principal.getId());
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
                                                          final Set<OAuth20GrantTypes> grantTypes,
                                                          final String clientSecret) {
        val registeredService = getRegisteredService(REDIRECT_URI, clientSecret, grantTypes);
        registeredService.setGenerateRefreshToken(generateRefreshToken);
        servicesManager.save(registeredService);
        return registeredService;
    }

    protected OAuthRegisteredService addRegisteredService(final Set<OAuth20GrantTypes> grantTypes,
                                                          final String clientId,
                                                          final String redirectUri) {
        val registeredService = getRegisteredService(redirectUri, clientId, UUID.randomUUID().toString(), grantTypes);
        servicesManager.save(registeredService);
        return registeredService;
    }

    protected OAuthRegisteredService addRegisteredService(final Set<OAuth20GrantTypes> grantTypes,
                                                          final String clientId,
                                                          final String clientSecret,
                                                          final String redirectUri) {
        val registeredService = getRegisteredService(redirectUri, clientId, clientSecret, grantTypes);
        servicesManager.save(registeredService);
        return registeredService;
    }

    protected OAuthRegisteredService addRegisteredService(final String redirectUri, final String clientSecret) {
        val registeredService = getRegisteredService(redirectUri, clientSecret, EnumSet.allOf(OAuth20GrantTypes.class));
        registeredService.setGenerateRefreshToken(true);
        servicesManager.save(registeredService);
        return registeredService;
    }

    protected OAuthRegisteredService addRegisteredService(final Set<OAuth20GrantTypes> grantTypes, final String clientSecret) {
        return addRegisteredService(false, grantTypes, clientSecret);
    }

    protected OAuthRegisteredService addRegisteredService() {
        return addRegisteredService(false, EnumSet.noneOf(OAuth20GrantTypes.class));
    }

    protected Pair<String, String> assertClientOK(final OAuthRegisteredService service,
                                                  final boolean refreshToken) throws Throwable {
        return assertClientOK(service, refreshToken, null);
    }

    protected Pair<String, String> assertClientOK(final OAuthRegisteredService service,
                                                  final boolean refreshToken,
                                                  final String scopes) throws Throwable {
        val principal = createPrincipal();
        val code = addCode(principal, service,
            org.springframework.util.StringUtils.commaDelimitedListToSet(scopes));
        LOGGER.debug("Added code [{}] for principal [{}]", code, principal);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(Locale.ENGLISH));
        val auth = service.getClientId() + ':' + service.getClientSecret();
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        val header = HttpConstants.BASIC_HEADER_PREFIX + value;
        mockRequest.addHeader(HttpConstants.AUTHORIZATION_HEADER, header);
        LOGGER.debug("Created header [{}] for client id [{}]", header, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
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

    protected OAuth20Code addCode(final Principal principal, final OAuthRegisteredService registeredService) throws Throwable {
        return addCodeWithChallenge(principal, registeredService, null, null);
    }

    protected OAuth20Code addCode(final Principal principal, final OAuthRegisteredService registeredService,
                                  final Collection<String> scopes) throws Throwable {
        return addCodeWithChallenge(principal, registeredService, null, null, scopes);
    }

    protected OAuth20Code addCodeWithChallenge(final Principal principal, final OAuthRegisteredService registeredService,
                                               final String codeChallenge, final String codeChallengeMethod) throws Throwable {
        return addCodeWithChallenge(principal, registeredService, codeChallenge, codeChallengeMethod, new ArrayList<>());
    }

    protected OAuth20Code addCodeWithChallenge(final Principal principal, final OAuthRegisteredService registeredService,
                                               final String codeChallenge, final String codeChallengeMethod,
                                               final Collection<String> scopes) throws Throwable {
        val authentication = getAuthentication(principal);
        val service = serviceFactory.createService(registeredService.getClientId());

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val code = oAuthCodeFactory.create(service, authentication,
            tgt, scopes, codeChallenge, codeChallengeMethod, registeredService.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(code);
        return code;
    }

    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.toDecodableCipher(accessTokenJwtBuilder).decode(token);
    }

    protected OAuth20RefreshToken addRefreshToken(final Principal principal,
                                                  final OAuthRegisteredService registeredService) throws Throwable {
        val authentication = getAuthentication(principal);
        val service = serviceFactory.createService(registeredService.getServiceId());
        val refreshToken = defaultRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), registeredService.getClientId(), StringUtils.EMPTY, new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.ticketRegistry.addTicket(refreshToken);
        return refreshToken;
    }

    protected OAuth20RefreshToken addRefreshToken(final Principal principal,
                                                  final OAuthRegisteredService registeredService,
                                                  final OAuth20AccessToken accessToken) throws Throwable {
        val authentication = getAuthentication(principal);
        val service = serviceFactory.createService(registeredService.getServiceId());
        val refreshToken = defaultRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), registeredService.getClientId(), accessToken.getId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.ticketRegistry.addTicket(refreshToken);
        return refreshToken;
    }

    protected OAuth20AccessToken addAccessToken(final Principal principal,
                                                final OAuthRegisteredService registeredService) throws Throwable {
        val code = addCode(principal, registeredService);
        return addAccessToken(principal, registeredService, code.getId());
    }

    protected OAuth20AccessToken addAccessToken(final Principal principal,
                                                final OAuthRegisteredService registeredService,
                                                final String codeId) throws Throwable {
        val authentication = getAuthentication(principal);
        val service = serviceFactory.createService(registeredService.getServiceId());
        val accessToken = defaultAccessTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), codeId, registeredService.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.ticketRegistry.addTicket(accessToken);
        return accessToken;
    }

    protected Pair<OAuth20AccessToken, OAuth20RefreshToken> assertRefreshTokenOk(final OAuthRegisteredService service) throws Throwable {
        val principal = createPrincipal();
        val refreshToken = addRefreshToken(principal, service);
        return assertRefreshTokenOk(service, refreshToken, principal);
    }

    protected Pair<OAuth20AccessToken, OAuth20RefreshToken> assertRefreshTokenOk(final OAuthRegisteredService service,
                                                                                 final OAuth20RefreshToken refreshToken,
                                                                                 final Principal principal) throws Throwable {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase(Locale.ENGLISH));
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, service.getClientSecret());
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
            ? ticketRegistry.getTicket(mv.getModel().get(OAuth20Constants.REFRESH_TOKEN).toString(), OAuth20RefreshToken.class)
            : refreshToken;

        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        val accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, OAuth20AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);

        return Pair.of(accessToken, newRefreshToken);
    }

    protected String randomServiceUrl() {
        return "https://app.example.org/%s".formatted(RandomUtils.randomAlphabetic(8));
    }

    protected ModelAndView generateAccessTokenResponseAndGetModelAndView(final OAuthRegisteredService registeredService) throws Throwable {
        return generateAccessTokenResponseAndGetModelAndView(registeredService,
            RegisteredServiceTestUtils.getAuthentication("casuser"), OAuth20GrantTypes.AUTHORIZATION_CODE);
    }

    protected ModelAndView generateAccessTokenResponseAndGetModelAndView(
        final OAuthRegisteredService registeredService,
        final Authentication authentication,
        final OAuth20GrantTypes grantType) throws Throwable {

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        return generateAccessTokenResponseAndGetModelAndView(registeredService, authentication, grantType, mockRequest);
    }

    protected ModelAndView generateAccessTokenResponseAndGetModelAndView(
        final OAuthRegisteredService registeredService,
        final Authentication authentication,
        final OAuth20GrantTypes grantType,
        final HttpServletRequest mockRequest) throws Throwable {

        val service = RegisteredServiceTestUtils.getService(SERVICE_URL);

        val mockResponse = new MockHttpServletResponse();
        val webContext = new JEEContext(mockRequest, mockResponse);
        val tokenRequestContext = buildAccessTokenRequestContext(registeredService, authentication, grantType, service, webContext);
        val generatedToken = oauthTokenGenerator.generate(tokenRequestContext);
        return generateAccessTokenResponse(registeredService, service, generatedToken, tokenRequestContext);
    }

    protected ModelAndView generateAccessTokenResponse(final OAuthRegisteredService registeredService,
                                                       final Service service,
                                                       final OAuth20TokenGeneratedResult generatedToken,
                                                       final AccessTokenRequestContext tokenRequestContext) {
        val result = OAuth20AccessTokenResponseResult
            .builder()
            .registeredService(registeredService)
            .responseType(OAuth20ResponseTypes.CODE)
            .service(service)
            .generatedToken(generatedToken)
            .responseType(tokenRequestContext.getResponseType())
            .grantType(tokenRequestContext.getGrantType())
            .requestedTokenType(tokenRequestContext.getRequestedTokenType())
            .build();
        return accessTokenResponseGenerator.generate(result);
    }

    protected AccessTokenRequestContext buildAccessTokenRequestContext(final OAuthRegisteredService registeredService,
                                                                       final Authentication authentication,
                                                                       final OAuth20GrantTypes grantType,
                                                                       final AbstractWebApplicationService service,
                                                                       final JEEContext webContext) throws Exception {
        return buildAccessTokenRequestContext(registeredService, authentication, grantType, service,
            new MockTicketGrantingTicket(authentication.getPrincipal().getId()), webContext);
    }

    protected AccessTokenRequestContext buildAccessTokenRequestContext(final OAuthRegisteredService registeredService,
                                                                       final Authentication authentication,
                                                                       final OAuth20GrantTypes grantType,
                                                                       final AbstractWebApplicationService service,
                                                                       final TicketGrantingTicket ticketGrantingTicket,
                                                                       final JEEContext webContext) throws Exception {
        return AccessTokenRequestContext
            .builder()
            .clientId(registeredService.getClientId())
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(grantType)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(ticketGrantingTicket)
            .generateRefreshToken(true)
            .claims(oauthRequestParameterResolver.resolveRequestClaims(webContext))
            .requestedTokenType(oauthRequestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.REQUESTED_TOKEN_TYPE)
                .map(OAuth20TokenExchangeTypes::from)
                .orElse(null))
            .subjectToken(oauthRequestParameterResolver.resolveRequestParameter(webContext, OAuth20Constants.SUBJECT_TOKEN)
                .map(token -> ticketRegistry.getTicket(token))
                .orElse(null))
            .build();
    }

    protected long getDefaultAccessTokenExpiration() {
        val seconds = casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds();
        return Beans.newDuration(seconds).toSeconds();
    }

    protected HttpSession storeProfileIntoSession(final HttpServletRequest request, final CommonProfile profile) {
        val session = request.getSession(true);
        assertNotNull(session);
        session.setAttribute("OauthOidcServerSupport%s".formatted(Pac4jConstants.USER_PROFILES),
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        return session;
    }

    @TestConfiguration(value = "OAuth20TestConfiguration", proxyBeanMethods = false)
    static class OAuth20TestConfiguration implements ComponentSerializationPlanConfigurer {
        @Autowired
        protected ApplicationContext applicationContext;

        @Bean
        public List inMemoryRegisteredServices() {
            val svc1 = RegisteredServiceTestUtils.getRegisteredService("^(https?|imaps?)://.*", OAuthRegisteredService.class);
            svc1.setClientId(UUID.randomUUID().toString());
            svc1.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

            val svc2 = RegisteredServiceTestUtils.getRegisteredService("https://example.org/jwt-access-token", OAuthRegisteredService.class);
            svc2.setClientId(UUID.randomUUID().toString());
            svc2.setJwtAccessToken(true);

            return CollectionUtils.wrapList(svc1, svc2);
        }

        @Override
        public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
            plan.registerSerializableClass(MockTicketGrantingTicket.class);
            plan.registerSerializableClass(MockServiceTicket.class);
        }
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasThrottlingAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasThymeleafAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasOAuth20AutoConfiguration.class,
        CasWebAppAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import({
        CasOAuth20AuthenticationEventExecutionPlanTestConfiguration.class,
        AbstractOAuth20Tests.OAuth20TestConfiguration.class})
    public static class SharedTestConfiguration {
    }
}
