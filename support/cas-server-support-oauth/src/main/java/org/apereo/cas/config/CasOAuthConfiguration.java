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
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.authenticator.OAuthClientAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuthUserAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
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
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
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
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.config.Config;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.http.UrlResolver;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.springframework.web.CallbackController;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
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
        return new OAuth20HandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor(), requiresAuthenticationAuthorizeInterceptor());
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

    private ExpirationPolicy accessTokenExpirationPolicy() {
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
    public UniqueTicketIdGenerator oAuthCodeIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    @RefreshScope
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
                oAuthValidator(), defaultAccessTokenFactory(), oauthPrincipalFactory(), webApplicationServiceFactory,
                oauthSecConfig(), callbackController(), callbackAuthorizeViewResolver(),
                profileScopeToAttributesFilter(), casProperties, ticketGrantingTicketCookieGenerator);
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
                defaultRefreshTokenFactory(),
                accessTokenResponseGenerator(),
                profileScopeToAttributesFilter(),
                casProperties,
                ticketGrantingTicketCookieGenerator,
                oauthCasAuthenticationBuilder(),
                centralAuthenticationService
        );
    }

    @ConditionalOnMissingBean(name = "profileController")
    @Bean
    @RefreshScope
    public OAuth20UserProfileControllerController profileController() {
        return new OAuth20UserProfileControllerController(servicesManager,
                ticketRegistry, oAuthValidator(), defaultAccessTokenFactory(),
                oauthPrincipalFactory(), webApplicationServiceFactory,
                profileScopeToAttributesFilter(), casProperties, ticketGrantingTicketCookieGenerator);
    }

    @ConditionalOnMissingBean(name = "authorizeController")
    @Bean
    @RefreshScope
    public OAuth20AuthorizeEndpointController authorizeController() {
        return new OAuth20AuthorizeEndpointController(
                servicesManager, ticketRegistry, oAuthValidator(), defaultAccessTokenFactory(),
                oauthPrincipalFactory(), webApplicationServiceFactory, defaultOAuthCodeFactory(),
                consentApprovalViewResolver(), profileScopeToAttributesFilter(), casProperties,
                ticketGrantingTicketCookieGenerator, oauthCasAuthenticationBuilder()
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

    @Bean
    @RefreshScope
    public CallbackController callbackController() {
        final CallbackController c = new CallbackController();
        c.setConfig(oauthSecConfig());
        return c;
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
            service.setId(Math.abs(new SecureRandom().nextLong()));
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
