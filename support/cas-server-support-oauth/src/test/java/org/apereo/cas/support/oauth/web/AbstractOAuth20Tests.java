package org.apereo.cas.support.oauth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
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
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.support.config.CasThrottlingConfiguration;
import org.junit.ClassRule;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractOAuth20Tests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(
    classes = {
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
@Slf4j
public abstract class AbstractOAuth20Tests {
    public static final String CONTEXT = "/oauth2.0/";
    public static final String CLIENT_ID = "1";
    public static final String CLIENT_SECRET = "secret";
    public static final String WRONG_CLIENT_SECRET = "wrongSecret";
    public static final String REDIRECT_URI = "http://someurl";
    public static final String OTHER_REDIRECT_URI = "http://someotherurl";
    public static final int TIMEOUT = 7200;
    public static final String ID = "1234";
    public static final String NAME = "attributeName";
    public static final String NAME2 = "attributeName2";
    public static final String VALUE = "attributeValue";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GOOD_USERNAME = "test";
    public static final String GOOD_PASSWORD = "test";
    public static final int DELTA = 2;

    public static final String ERROR_EQUALS = "error=";

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("accessTokenController")
    protected OAuth20AccessTokenEndpointController oAuth20AccessTokenController;

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

    @TestConfiguration
    public static class OAuthTestConfiguration implements ComponentSerializationPlanConfigurator, InitializingBean {
        @Autowired
        protected ApplicationContext applicationContext;

        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            init();
        }

        @Bean
        public List inMemoryRegisteredServices() {
            final var svc = RegisteredServiceTestUtils.getRegisteredService("^(https?|imaps?)://.*");
            svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
            final List l = new ArrayList();
            l.add(svc);
            return l;
        }

        @Override
        public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
            plan.registerSerializableClass(MockTicketGrantingTicket.class);
            plan.registerSerializableClass(MockServiceTicket.class);
        }
    }

    protected static Principal createPrincipal() {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final var list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        return CoreAuthenticationTestUtils.getPrincipal(ID, map);
    }

    protected OAuthRegisteredService addRegisteredService() {
        return addRegisteredService(false);
    }

    protected OAuthRegisteredService addRegisteredService(final boolean generateRefreshToken) {
        final var registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        registeredService.setGenerateRefreshToken(generateRefreshToken);
        servicesManager.save(registeredService);
        return registeredService;
    }

    protected OAuthCode addCode(final Principal principal, final OAuthRegisteredService registeredService) {
        final var authentication = getAuthentication(principal);
        final var factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getClientId());
        final var code = oAuthCodeFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(code);
        return code;
    }

    protected RefreshToken addRefreshToken(final Principal principal, final OAuthRegisteredService registeredService) {
        final var authentication = getAuthentication(principal);
        final var factory = new WebApplicationServiceFactory();
        final Service service = factory.createService(registeredService.getServiceId());
        final var refreshToken = oAuthRefreshTokenFactory.create(service, authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(refreshToken);
        return refreshToken;
    }

    protected static OAuthRegisteredService getRegisteredService(final String serviceId, final String secret) {
        final var registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName("The registered service name");
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setClientSecret(secret);
        registeredServiceImpl.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        return registeredServiceImpl;
    }

    protected void clearAllServices() {
        final var col = servicesManager.getAllServices();
        col.forEach(r -> servicesManager.delete(r.getId()));
        servicesManager.load();
    }

    protected static Authentication getAuthentication(final Principal principal) {
        final CredentialMetaData metadata = new BasicCredentialMetaData(
            new BasicIdentifiableCredential(principal.getId()));
        final AuthenticationHandlerExecutionResult handlerResult = new DefaultAuthenticationHandlerExecutionResult(principal.getClass().getCanonicalName(),
            metadata, principal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principal)
            .setAuthenticationDate(ZonedDateTime.now())
            .addCredential(metadata)
            .addSuccess(principal.getClass().getCanonicalName(), handlerResult)
            .build();
    }

    protected Pair<String, String> internalVerifyClientOK(final OAuthRegisteredService service,
                                                          final boolean refreshToken, final boolean json) throws Exception {

        final var principal = createPrincipal();
        final var code = addCode(principal, service);

        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase());
        final var auth = CLIENT_ID + ':' + CLIENT_SECRET;
        final var value = EncodingUtils.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        mockRequest.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX + value);

        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);

        mockRequest.setParameter(OAuth20Constants.CODE, code.getId());
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertNull(this.ticketRegistry.getTicket(code.getId()));
        assertEquals(HttpStatus.SC_OK, mockResponse.getStatus());
        final var body = mockResponse.getContentAsString();

        final String accessTokenId;
        String refreshTokenId = null;

        if (json) {
            assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
            assertTrue(body.contains('"' + OAuth20Constants.ACCESS_TOKEN + "\":\"AT-"));

            final var results = MAPPER.readValue(body, Map.class);
            if (refreshToken) {
                assertTrue(body.contains('"' + OAuth20Constants.REFRESH_TOKEN + "\":\"RT-"));
                refreshTokenId = results.get(OAuth20Constants.REFRESH_TOKEN).toString();
            }
            assertTrue(body.contains('"' + OAuth20Constants.EXPIRES_IN + "\":"));
            accessTokenId = results.get(OAuth20Constants.ACCESS_TOKEN).toString();
        } else {
            assertEquals(MediaType.TEXT_PLAIN_VALUE, mockResponse.getContentType());
            assertTrue(body.contains(OAuth20Constants.ACCESS_TOKEN + "=AT-"));
            if (refreshToken) {
                assertTrue(body.contains(OAuth20Constants.REFRESH_TOKEN + "=RT-"));
                refreshTokenId = Arrays.stream(body.split("&"))
                    .filter(f -> f.startsWith(OAuth20Constants.REFRESH_TOKEN))
                    .map(f -> StringUtils.remove(f, OAuth20Constants.REFRESH_TOKEN + '='))
                    .findFirst()
                    .get();
            }
            assertTrue(body.contains(OAuth20Constants.EXPIRES_IN + '='));
            accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + '=', "&");
        }

        final var accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        final var timeLeft = getTimeLeft(body, refreshToken, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);

        return Pair.of(accessTokenId, refreshTokenId);
    }

    protected static int getTimeLeft(final String body, final boolean refreshToken, final boolean json) {
        final int timeLeft;
        if (json) {
            if (refreshToken) {
                timeLeft = Integer.parseInt(StringUtils.substringBetween(body, OAuth20Constants.EXPIRES_IN + "\":", ","));
            } else {
                timeLeft = Integer.parseInt(StringUtils.substringBetween(body, OAuth20Constants.EXPIRES_IN + "\":", "}"));
            }
        } else {
            if (refreshToken) {
                timeLeft = Integer.parseInt(StringUtils.substringBetween(body, '&' + OAuth20Constants.EXPIRES_IN + '=',
                    '&' + OAuth20Constants.REFRESH_TOKEN));
            } else {
                timeLeft = Integer.parseInt(StringUtils.substringAfter(body, '&' + OAuth20Constants.EXPIRES_IN + '='));
            }
        }
        return timeLeft;
    }

    protected Pair<AccessToken, RefreshToken> internalVerifyRefreshTokenOk(final OAuthRegisteredService service, final boolean json) throws Exception {
        final var principal = createPrincipal();
        final var refreshToken = addRefreshToken(principal, service);
        return internalVerifyRefreshTokenOk(service, json, refreshToken, principal);
    }


    protected Pair<AccessToken, RefreshToken> internalVerifyRefreshTokenOk(final OAuthRegisteredService service, final boolean json,
                                                                           final RefreshToken refreshToken, final Principal principal) throws Exception {
        final var mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase());
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        final var mockResponse = new MockHttpServletResponse();
        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        final var body = mockResponse.getContentAsString();

        final String accessTokenId;
        if (json) {
            final var results = MAPPER.readValue(body, Map.class);

            assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
            assertTrue(body.contains('"' + OAuth20Constants.ACCESS_TOKEN + "\":\"AT-"));
            assertFalse(body.contains('"' + OAuth20Constants.REFRESH_TOKEN + "\":\"RT-"));
            assertTrue(body.contains('"' + OAuth20Constants.EXPIRES_IN + "\":"));
            accessTokenId = results.get(OAuth20Constants.ACCESS_TOKEN).toString();
        } else {
            assertEquals(MediaType.TEXT_PLAIN_VALUE, mockResponse.getContentType());
            assertTrue(body.contains(OAuth20Constants.ACCESS_TOKEN + '='));
            assertFalse(body.contains(OAuth20Constants.REFRESH_TOKEN + '='));
            assertTrue(body.contains(OAuth20Constants.EXPIRES_IN + '='));
            accessTokenId = StringUtils.substringBetween(body, OAuth20Constants.ACCESS_TOKEN + '=', "&");
        }

        final var accessToken = this.ticketRegistry.getTicket(accessTokenId, AccessToken.class);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        final var timeLeft = getTimeLeft(body, false, json);
        assertTrue(timeLeft >= TIMEOUT - 10 - DELTA);

        return Pair.of(accessToken, refreshToken);
    }
}
