package org.apereo.cas.support.oauth.web;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
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
import org.apereo.cas.config.CasCoreComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuthAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuthComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.config.CasOAuthThrottleConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasThrottlingConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20DeviceUserCodeApprovalEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.web.config.CasCookieConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.junit.Rule;
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
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractOAuth20Tests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
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
    CasOAuthConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreConfiguration.class,
    CasCookieConfiguration.class,
    CasOAuthComponentSerializationConfiguration.class,
    CasOAuthThrottleConfiguration.class,
    CasThrottlingConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasOAuthAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreComponentSerializationConfiguration.class,
    CasOAuth20TestAuthenticationEventExecutionPlanConfiguration.class,
    CasCoreUtilSerializationConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    AbstractOAuth20Tests.OAuthTestConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class})
@DirtiesContext
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public abstract class AbstractOAuth20Tests {
    public static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static final String CONTEXT = OAuth20Constants.BASE_OAUTH20_URL + '/';
    public static final String CLIENT_ID = "1";
    public static final String CLIENT_SECRET = "secret";
    public static final String WRONG_CLIENT_SECRET = "wrongSecret";
    public static final String REDIRECT_URI = "http://someurl";
    public static final String OTHER_REDIRECT_URI = "http://someotherurl";
    public static final int TIMEOUT = 7200;
    public static final String ID = "casuser";
    public static final String NAME = "attributeName";
    public static final String ATTRIBUTES_PARAM = "attributes";
    public static final String NAME2 = "attributeName2";
    public static final String VALUE = "attributeValue";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GOOD_USERNAME = "test";
    public static final String GOOD_PASSWORD = "test";
    public static final int DELTA = 2;

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("accessTokenController")
    protected OAuth20AccessTokenEndpointController controller;

    @Autowired
    @Qualifier("deviceUserCodeApprovalEndpointController")
    protected OAuth20DeviceUserCodeApprovalEndpointController deviceController;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    protected SecurityInterceptor requiresAuthenticationInterceptor;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuthCodeFactory oAuthCodeFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    protected RefreshTokenFactory oAuthRefreshTokenFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    protected static Principal createPrincipal() {
        val map = new HashMap<String, Object>();
        map.put(NAME, VALUE);
        val list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        return CoreAuthenticationTestUtils.getPrincipal(ID, map);
    }

    protected OAuthRegisteredService getRegisteredService(final String serviceId,
                                                          final String secret,
                                                          final Set<OAuth20GrantTypes> grantTypes) {
        val registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName("The registered service name");
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setClientSecret(secret);
        registeredServiceImpl.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        registeredServiceImpl.setSupportedGrantTypes(
                grantTypes.stream().map(OAuth20GrantTypes::getType).collect(Collectors.toCollection(HashSet::new)));
        return registeredServiceImpl;
    }

    protected OAuthRegisteredService addRegisteredService(final Set<OAuth20GrantTypes> grantTypes) {
        return addRegisteredService(false, grantTypes);
    }

    protected OAuthRegisteredService addRegisteredService(final Set<OAuth20GrantTypes> grantTypes, final String clientSecret) {
        return addRegisteredService(false, grantTypes, clientSecret);
    }

    protected OAuthRegisteredService addRegisteredService() {
        return addRegisteredService(false, new HashSet<>());
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


    protected static Authentication getAuthentication(final Principal principal) {
        val metadata = new BasicCredentialMetaData(
            new BasicIdentifiableCredential(principal.getId()));
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult(principal.getClass().getCanonicalName(),
            metadata, principal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principal)
            .setAuthenticationDate(ZonedDateTime.now())
            .addCredential(metadata)
            .addSuccess(principal.getClass().getCanonicalName(), handlerResult)
            .build();
    }

    protected OAuthCode addCode(final Principal principal, final OAuthRegisteredService registeredService) {
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getClientId());
        val code = oAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(), null, null);
        this.ticketRegistry.addTicket(code);
        return code;
    }

    protected RefreshToken addRefreshToken(final Principal principal, final OAuthRegisteredService registeredService) {
        val authentication = getAuthentication(principal);
        val factory = new WebApplicationServiceFactory();
        val service = factory.createService(registeredService.getServiceId());
        val refreshToken = oAuthRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(refreshToken);
        return refreshToken;
    }

    protected void clearAllServices() {
        val col = servicesManager.getAllServices();
        col.forEach(r -> servicesManager.delete(r.getId()));
        servicesManager.load();
    }

    protected Pair<String, String> internalVerifyClientOK(final OAuthRegisteredService service,
                                                          final boolean refreshToken) throws Exception {
        return internalVerifyClientOK(service, refreshToken, null);
    }

    protected Pair<String, String> internalVerifyClientOK(final OAuthRegisteredService service,
                                                          final boolean refreshToken,
                                                          final String scopes) throws Exception {

        val principal = createPrincipal();
        val code = addCode(principal, service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        val auth = CLIENT_ID + ':' + CLIENT_SECRET;
        val value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        mockRequest.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);

        if (StringUtils.isNotBlank(scopes)) {
            mockRequest.setParameter(OAuth20Constants.SCOPE, scopes);
        }

        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertNull(this.ticketRegistry.getTicket(code.getId()));
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());

        var accessTokenId = StringUtils.EMPTY;
        var refreshTokenId = StringUtils.EMPTY;

        val model = mv.getModel();
        assertTrue(model.containsKey(OAuth20Constants.ACCESS_TOKEN));

        if (refreshToken) {
            assertTrue(model.containsKey(OAuth20Constants.REFRESH_TOKEN));
            refreshTokenId = model.get(OAuth20Constants.REFRESH_TOKEN).toString();
        }
        assertTrue(model.containsKey(OAuth20Constants.EXPIRES_IN));
        accessTokenId = model.get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(model.get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);

        return Pair.of(accessTokenId, refreshTokenId);
    }

    protected Pair<AccessToken, RefreshToken> internalVerifyRefreshTokenOk(final OAuthRegisteredService service) throws Exception {
        val principal = createPrincipal();
        val refreshToken = addRefreshToken(principal, service);
        return internalVerifyRefreshTokenOk(service, refreshToken, principal);
    }

    protected Pair<AccessToken, RefreshToken> internalVerifyRefreshTokenOk(final OAuthRegisteredService service,
                                                                           final RefreshToken refreshToken, final Principal principal) throws Exception {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        val mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = controller.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());

        var accessTokenId = StringUtils.EMPTY;

        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertFalse(mv.getModel().containsKey(OAuth20Constants.REFRESH_TOKEN));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
        accessTokenId = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();

        val accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        val timeLeft = Integer.parseInt(mv.getModel().get(OAuth20Constants.EXPIRES_IN).toString());
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);

        return Pair.of(accessToken, refreshToken);
    }

    @TestConfiguration
    public static class OAuthTestConfiguration implements ComponentSerializationPlanConfigurator, InitializingBean {
        @Autowired
        protected ApplicationContext applicationContext;

        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }

        @Override
        public void afterPropertiesSet() {
            init();
        }

        @Bean
        public List inMemoryRegisteredServices() {
            val svc = RegisteredServiceTestUtils.getRegisteredService("^(https?|imaps?)://.*");
            svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
            return CollectionUtils.wrapList(svc);
        }

        @Override
        public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
            plan.registerSerializableClass(MockTicketGrantingTicket.class);
            plan.registerSerializableClass(MockServiceTicket.class);
        }
    }
}
