package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.authenticator.OAuthClientAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuthUserAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20AuthorizationCodeResponseTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20ClientCredentialsGrantTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20IdTokenResponseTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20PasswordGrantTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20RefreshTokenGrantTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20RequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20TokenResponseTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20CallbackAuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileControllerController;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenAuthorizationCodeGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenClientCredentialsGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenPasswordGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRefreshTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ClientCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResourceOwnerCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.DefaultAccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicy;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.config.Config;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.http.UrlResolver;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apereo.cas.support.oauth.OAuth20Constants.*;

/**
 * This this {@link CasOAuthConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oauthConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthConfiguration extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasOAuthConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @ConditionalOnMissingBean(name = "accessTokenResponseGenerator")
    @Bean
    public AccessTokenResponseGenerator accessTokenResponseGenerator() {
        return new OAuth20AccessTokenResponseGenerator();
    }

    @ConditionalOnMissingBean(name = "oauthCasClientRedirectActionBuilder")
    @Bean
    public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new OAuth20DefaultCasClientRedirectActionBuilder();
    }

    @RefreshScope
    @Bean
    public UrlResolver casCallbackUrlResolver() {
        return new OAuth20CasCallbackUrlResolver(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()));
    }

    @RefreshScope
    @Bean
    public Config oauthSecConfig() {
        final CasConfiguration cfg = new CasConfiguration(casProperties.getServer().getLoginUrl());
        final CasClient oauthCasClient = new CasClient(cfg);
        oauthCasClient.setRedirectActionBuilder(webContext -> oauthCasClientRedirectActionBuilder().build(oauthCasClient, webContext));
        oauthCasClient.setName(Authenticators.CAS_OAUTH_CLIENT);
        oauthCasClient.setUrlResolver(casCallbackUrlResolver());

        final Authenticator authenticator = oAuthClientAuthenticator();
        final DirectBasicAuthClient basicAuthClient = new DirectBasicAuthClient(authenticator);
        basicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);

        final DirectFormClient directFormClient = new DirectFormClient(authenticator);
        directFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM);
        directFormClient.setUsernameParameter(CLIENT_ID);
        directFormClient.setPasswordParameter(CLIENT_SECRET);

        final DirectFormClient userFormClient = new DirectFormClient(oAuthUserAuthenticator());
        userFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_USER_FORM);
        return new Config(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()),
                oauthCasClient, basicAuthClient, directFormClient, userFormClient);
    }

    @ConditionalOnMissingBean(name = "requiresAuthenticationAuthorizeInterceptor")
    @Bean
    @RefreshScope
    public SecurityInterceptor requiresAuthenticationAuthorizeInterceptor() {
        return new SecurityInterceptor(oauthSecConfig(), Authenticators.CAS_OAUTH_CLIENT);
    }

    @ConditionalOnMissingBean(name = "consentApprovalViewResolver")
    @Bean
    @RefreshScope
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OAuth20ConsentApprovalViewResolver(casProperties);
    }

    @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
    @Bean
    @RefreshScope
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OAuth20CallbackAuthorizeViewResolver() {
        };
    }

    @ConditionalOnMissingBean(name = "requiresAuthenticationAccessTokenInterceptor")
    @Bean
    @RefreshScope
    public SecurityInterceptor requiresAuthenticationAccessTokenInterceptor() {
        final String clients = Stream.of(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
                Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
                Authenticators.CAS_OAUTH_CLIENT_USER_FORM).collect(Collectors.joining(","));
        return new SecurityInterceptor(oauthSecConfig(), clients);
    }

    @ConditionalOnMissingBean(name = "oauthInterceptor")
    @Bean
    @RefreshScope
    public HandlerInterceptorAdapter oauthInterceptor() {
        final String throttler = casProperties.getAuthn().getOauth().getThrottler();
        final OAuth20HandlerInterceptorAdapter oAuth20HandlerInterceptorAdapter = new OAuth20HandlerInterceptorAdapter(
                requiresAuthenticationAccessTokenInterceptor(), requiresAuthenticationAuthorizeInterceptor(), accessTokenGrantRequestExtractors());

        if ("neverThrottle".equalsIgnoreCase(throttler)) {
            LOGGER.debug("Authentication throttling is disabled for OAuth");
            return oAuth20HandlerInterceptorAdapter;
        }
        return getHandlerInterceptorForThrottling(oAuth20HandlerInterceptorAdapter);
    }

    private HandlerInterceptorAdapter getHandlerInterceptorForThrottling(final OAuth20HandlerInterceptorAdapter oAuth20HandlerInterceptorAdapter) {
        final String throttler = casProperties.getAuthn().getOauth().getThrottler();
        LOGGER.debug("Locating authentication throttler instance [{}] for OAuth", throttler);
        final HandlerInterceptor throttledInterceptor = this.applicationContext.getBean(throttler, HandlerInterceptor.class);
        
        final String throttledUrl = OAuth20Constants.BASE_OAUTH20_URL.concat("/")
                .concat(OAuth20Constants.ACCESS_TOKEN_URL + "|" + OAuth20Constants.TOKEN_URL);
        final Pattern pattern = RegexUtils.createPattern(throttledUrl);
        LOGGER.debug("Authentication throttler instance for OAuth shall intercept the URL pattern [{}]", pattern.pattern());

        final HandlerInterceptorAdapter throttledInterceptorAdapter = new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
                if (RegexUtils.matches(pattern, request.getServletPath()) && !throttledInterceptor.preHandle(request, response, handler)) {
                    LOGGER.trace("OAuth authentication throttler prevented the request at [{}]", request.getServletPath());
                    return false;
                }
                return oAuth20HandlerInterceptorAdapter.preHandle(request, response, handler);
            }

            @Override
            public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
                                   final ModelAndView modelAndView) throws Exception {
                if (RegexUtils.matches(pattern, request.getServletPath())) {
                    LOGGER.trace("OAuth authentication throttler post-processing the request at [{}]", request.getServletPath());
                    throttledInterceptor.postHandle(request, response, handler, modelAndView);
                }
            }
        };
        return throttledInterceptorAdapter;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oauthInterceptor()).addPathPatterns(BASE_OAUTH20_URL.concat("/").concat("*"));
    }

    @Bean
    @RefreshScope
    public OAuth20CasClientRedirectActionBuilder defaultOAuthCasClientRedirectActionBuilder() {
        return new OAuth20DefaultCasClientRedirectActionBuilder();
    }

    @ConditionalOnMissingBean(name = "oAuthClientAuthenticator")
    @Bean
    @RefreshScope
    public Authenticator<UsernamePasswordCredentials> oAuthClientAuthenticator() {
        return new OAuthClientAuthenticator(oAuthValidator(), this.servicesManager);
    }

    @ConditionalOnMissingBean(name = "oAuthUserAuthenticator")
    @Bean
    @RefreshScope
    public Authenticator<UsernamePasswordCredentials> oAuthUserAuthenticator() {
        return new OAuthUserAuthenticator(authenticationSystemSupport, servicesManager, webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "oAuthValidator")
    @Bean
    @RefreshScope
    public OAuth20Validator oAuthValidator() {
        return new OAuth20Validator(webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "oauthAccessTokenResponseGenerator")
    @Bean
    @RefreshScope
    public AccessTokenResponseGenerator oauthAccessTokenResponseGenerator() {
        return new OAuth20AccessTokenResponseGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultAccessTokenFactory")
    public AccessTokenFactory defaultAccessTokenFactory() {
        return new DefaultAccessTokenFactory(accessTokenIdGenerator(), accessTokenExpirationPolicy());
    }

    @Bean
    @ConditionalOnMissingBean(name = "accessTokenExpirationPolicy")
    public ExpirationPolicy accessTokenExpirationPolicy() {
        final OAuthProperties oauth = casProperties.getAuthn().getOauth();
        return new OAuthAccessTokenExpirationPolicy(
                oauth.getAccessToken().getMaxTimeToLiveInSeconds(),
                oauth.getAccessToken().getTimeToKillInSeconds()
        );
    }

    private ExpirationPolicy oAuthCodeExpirationPolicy() {
        final OAuthProperties oauth = casProperties.getAuthn().getOauth();
        return new OAuthCodeExpirationPolicy(oauth.getCode().getNumberOfUses(), oauth.getCode().getTimeToKillInSeconds());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oAuthCodeIdGenerator")
    public UniqueTicketIdGenerator oAuthCodeIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "refreshTokenIdGenerator")
    public UniqueTicketIdGenerator refreshTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultOAuthCodeFactory")
    public OAuthCodeFactory defaultOAuthCodeFactory() {
        return new DefaultOAuthCodeFactory(oAuthCodeIdGenerator(), oAuthCodeExpirationPolicy());
    }

    @ConditionalOnMissingBean(name = "profileScopeToAttributesFilter")
    @Bean
    public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
        return new DefaultOAuth20ProfileScopeToAttributesFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "callbackAuthorizeController")
    @RefreshScope
    public OAuth20CallbackAuthorizeEndpointController callbackAuthorizeController() {
        return new OAuth20CallbackAuthorizeEndpointController(servicesManager, ticketRegistry,
                oAuthValidator(), defaultAccessTokenFactory(), oauthPrincipalFactory(),
                webApplicationServiceFactory,
                oauthSecConfig(), callbackAuthorizeViewResolver(),
                profileScopeToAttributesFilter(), casProperties, ticketGrantingTicketCookieGenerator);
    }

    @ConditionalOnMissingBean(name = "oauthTokenGenerator")
    @Bean
    @RefreshScope
    public OAuth20TokenGenerator oauthTokenGenerator() {
        return new OAuth20DefaultTokenGenerator(defaultAccessTokenFactory(), ticketRegistry, defaultRefreshTokenFactory());
    }

    @Bean
    public Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors() {
        final BaseAccessTokenGrantRequestExtractor authzCodeExt =
                new AccessTokenAuthorizationCodeGrantRequestExtractor(servicesManager, ticketRegistry,
                        centralAuthenticationService, casProperties.getAuthn().getOauth());

        final BaseAccessTokenGrantRequestExtractor refreshTokenExt =
                new AccessTokenRefreshTokenGrantRequestExtractor(servicesManager, ticketRegistry,
                        centralAuthenticationService, casProperties.getAuthn().getOauth());

        final BaseAccessTokenGrantRequestExtractor pswExt =
                new AccessTokenPasswordGrantRequestExtractor(servicesManager, ticketRegistry,
                        oauthCasAuthenticationBuilder(), centralAuthenticationService,
                        casProperties.getAuthn().getOauth());

        final BaseAccessTokenGrantRequestExtractor credsExt =
                new AccessTokenClientCredentialsGrantRequestExtractor(servicesManager, ticketRegistry,
                        oauthCasAuthenticationBuilder(), centralAuthenticationService,
                        casProperties.getAuthn().getOauth());

        return CollectionUtils.wrapList(authzCodeExt, refreshTokenExt, pswExt, credsExt);
    }

    @ConditionalOnMissingBean(name = "accessTokenController")
    @Bean
    @RefreshScope
    public OAuth20AccessTokenEndpointController accessTokenController() {
        return new OAuth20AccessTokenEndpointController(
                servicesManager,
                ticketRegistry,
                oAuthValidator(),
                defaultAccessTokenFactory(),
                oauthPrincipalFactory(),
                webApplicationServiceFactory,
                oauthTokenGenerator(),
                accessTokenResponseGenerator(),
                profileScopeToAttributesFilter(),
                casProperties,
                ticketGrantingTicketCookieGenerator,
                accessTokenExpirationPolicy(),
                accessTokenGrantRequestExtractors()
        );
    }

    @ConditionalOnMissingBean(name = "oauthUserProfileViewRenderer")
    @Bean
    @RefreshScope
    public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer() {
        return new OAuth20DefaultUserProfileViewRenderer(casProperties.getAuthn().getOauth());
    }

    @ConditionalOnMissingBean(name = "profileController")
    @Bean
    @RefreshScope
    public OAuth20UserProfileControllerController profileController() {
        return new OAuth20UserProfileControllerController(servicesManager,
                ticketRegistry, oAuthValidator(), defaultAccessTokenFactory(),
                oauthPrincipalFactory(), webApplicationServiceFactory,
                profileScopeToAttributesFilter(), casProperties,
                ticketGrantingTicketCookieGenerator,
                oauthUserProfileViewRenderer());
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationResponseBuilders")
    @Bean
    @RefreshScope
    public Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders() {
        final Map<String, OAuth20AuthorizationResponseBuilder> builders =
                this.applicationContext.getBeansOfType(OAuth20AuthorizationResponseBuilder.class, false, true);
        return new HashSet<>(builders.values());
    }


    @ConditionalOnMissingBean(name = "oauthRequestValidators")
    @Bean
    @RefreshScope
    public Set<OAuth20RequestValidator> oauthRequestValidators() {
        final Map<String, OAuth20RequestValidator> builders =
                this.applicationContext.getBeansOfType(OAuth20RequestValidator.class, false, true);
        return new HashSet<>(builders.values());
    }

    @ConditionalOnMissingBean(name = "oauthClientCredentialsGrantTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20RequestValidator oauthClientCredentialsGrantTypeRequestValidator() {
        return new OAuth20ClientCredentialsGrantTypeRequestValidator(servicesManager, oAuthValidator());
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20RequestValidator oauthAuthorizationCodeResponseTypeRequestValidator() {
        return new OAuth20AuthorizationCodeResponseTypeRequestValidator(servicesManager, oAuthValidator());
    }

    @ConditionalOnMissingBean(name = "oauthTokenResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20RequestValidator oauthTokenResponseTypeRequestValidator() {
        return new OAuth20TokenResponseTypeRequestValidator(servicesManager, oAuthValidator());
    }

    @ConditionalOnMissingBean(name = "oauthIdTokenResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20RequestValidator oauthIdTokenResponseTypeRequestValidator() {
        return new OAuth20IdTokenResponseTypeRequestValidator(servicesManager, oAuthValidator());
    }

    @ConditionalOnMissingBean(name = "oauthPasswordGrantTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20RequestValidator oauthPasswordGrantTypeRequestValidator() {
        return new OAuth20PasswordGrantTypeRequestValidator(servicesManager, oAuthValidator());
    }

    @ConditionalOnMissingBean(name = "oauthRefreshTokenGrantTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20RequestValidator oauthRefreshTokenGrantTypeRequestValidator() {
        return new OAuth20RefreshTokenGrantTypeRequestValidator(servicesManager, oAuthValidator());
    }

    @ConditionalOnMissingBean(name = "oauthResourceOwnerCredentialsResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthResourceOwnerCredentialsResponseBuilder() {
        return new OAuth20ResourceOwnerCredentialsResponseBuilder(accessTokenResponseGenerator(), oauthTokenGenerator(),
                accessTokenExpirationPolicy());
    }

    @ConditionalOnMissingBean(name = "oauthClientCredentialsResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthClientCredentialsResponseBuilder() {
        return new OAuth20ClientCredentialsResponseBuilder(accessTokenResponseGenerator(),
                oauthTokenGenerator(), accessTokenExpirationPolicy());
    }

    @ConditionalOnMissingBean(name = "oauthTokenResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthTokenResponseBuilder() {
        return new OAuth20TokenAuthorizationResponseBuilder(oauthTokenGenerator(), accessTokenExpirationPolicy());
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthAuthorizationCodeResponseBuilder() {
        return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(ticketRegistry, defaultOAuthCodeFactory());
    }

    @ConditionalOnMissingBean(name = "authorizeController")
    @Bean
    @RefreshScope
    public OAuth20AuthorizeEndpointController authorizeController() {
        return new OAuth20AuthorizeEndpointController(
                servicesManager, ticketRegistry, oAuthValidator(), defaultAccessTokenFactory(),
                oauthPrincipalFactory(), webApplicationServiceFactory, defaultOAuthCodeFactory(),
                consentApprovalViewResolver(), profileScopeToAttributesFilter(), casProperties,
                ticketGrantingTicketCookieGenerator, oauthCasAuthenticationBuilder(),
                oauthAuthorizationResponseBuilders(), oauthRequestValidators()
        );
    }

    @ConditionalOnMissingBean(name = "oauthPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory oauthPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultRefreshTokenFactory")
    public RefreshTokenFactory defaultRefreshTokenFactory() {
        return new DefaultRefreshTokenFactory(refreshTokenIdGenerator(), refreshTokenExpirationPolicy());
    }

    private ExpirationPolicy refreshTokenExpirationPolicy() {
        return new OAuthRefreshTokenExpirationPolicy(casProperties.getAuthn().getOauth().getRefreshToken().getTimeToKillInSeconds());
    }

    @ConditionalOnMissingBean(name = "oauthCasAuthenticationBuilder")
    @Bean
    @RefreshScope
    public OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder() {
        return new OAuth20CasAuthenticationBuilder(oauthPrincipalFactory(), webApplicationServiceFactory,
                profileScopeToAttributesFilter(), casProperties);
    }

    @ConditionalOnMissingBean(name = "accessTokenIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator accessTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @PostConstruct
    public void initializeServletApplicationContext() {
        final String oAuthCallbackUrl = casProperties.getServer().getPrefix() + BASE_OAUTH20_URL + '/' + CALLBACK_AUTHORIZE_URL_DEFINITION;

        final Service callbackService = this.webApplicationServiceFactory.createService(oAuthCallbackUrl);
        final RegisteredService svc = servicesManager.findServiceBy(callbackService);

        if (svc == null || !svc.getServiceId().equals(oAuthCallbackUrl)) {
            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(Math.abs(RandomUtils.getInstanceNative().nextLong()));
            service.setEvaluationOrder(0);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("OAuth Authentication Callback Request URL");
            service.setServiceId(oAuthCallbackUrl);
            service.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());

            servicesManager.save(service);
            servicesManager.load();
        }
    }
}
