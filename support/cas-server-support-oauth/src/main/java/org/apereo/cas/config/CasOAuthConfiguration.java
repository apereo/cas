package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.DefaultOAuthCasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.OAuthCasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuthClientAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuthUserAuthenticator;
import org.apereo.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.apereo.cas.support.oauth.validator.OAuth20AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenController;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.OAuth20AuthorizeController;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeController;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.OAuth20ProfileController;
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
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.jasig.cas.client.util.URIBuilder;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.http.CallbackUrlResolver;
import org.pac4j.core.redirect.RedirectAction;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apereo.cas.support.oauth.OAuthConstants.BASE_OAUTH20_URL;

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
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @ConditionalOnMissingBean(name = "accessTokenResponseGenerator")
    public AccessTokenResponseGenerator accessTokenResponseGenerator() {
        return new OAuth20AccessTokenResponseGenerator();
    }

    @ConditionalOnMissingBean(name = "oauthCasClientRedirectActionBuilder")
    public OAuthCasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new DefaultOAuthCasClientRedirectActionBuilder();
    }

    @Bean
    public Config oauthSecConfig() {
        final CasConfiguration cfg = new CasConfiguration(casProperties.getServer().getLoginUrl());
        final CasClient oauthCasClient = new CasClient(cfg) {
            @Override
            protected RedirectAction retrieveRedirectAction(final WebContext context) {
                return oauthCasClientRedirectActionBuilder().build(this, context);
            }
        };

        oauthCasClient.setName(Authenticators.CAS_OAUTH_CLIENT);
        oauthCasClient.setCallbackUrlResolver(buildOAuthCasCallbackUrlResolver());

        final Authenticator authenticator = oAuthClientAuthenticator();
        final DirectBasicAuthClient basicAuthClient = new DirectBasicAuthClient(authenticator);
        basicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);

        final DirectFormClient directFormClient = new DirectFormClient(authenticator);
        directFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM);
        directFormClient.setUsernameParameter(OAuthConstants.CLIENT_ID);
        directFormClient.setPasswordParameter(OAuthConstants.CLIENT_SECRET);

        final DirectFormClient userFormClient = new DirectFormClient(oAuthUserAuthenticator());
        userFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_USER_FORM);

        final String callbackUrl = casProperties.getServer().getPrefix().concat(OAuthConstants.BASE_OAUTH20_URL
                + '/' + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        return new Config(callbackUrl, oauthCasClient, basicAuthClient, directFormClient, userFormClient);
    }

    private CallbackUrlResolver buildOAuthCasCallbackUrlResolver() {
        return (url, context) -> {
            final String callbackUrl = casProperties.getServer().getPrefix().concat(OAuthConstants.BASE_OAUTH20_URL
                    + '/' + OAuthConstants.CALLBACK_AUTHORIZE_URL);
            if (url.startsWith(callbackUrl)) {
                final URIBuilder builder = new URIBuilder(url);
                final URIBuilder builderContext = new URIBuilder(context.getFullRequestURL());
                Optional<URIBuilder.BasicNameValuePair> parameter = builderContext.getQueryParams()
                        .stream().filter(p -> p.getName().equals(OAuthConstants.CLIENT_ID))
                        .findFirst();

                if (parameter.isPresent()) {
                    builder.addParameter(parameter.get().getName(), parameter.get().getValue());
                }
                parameter = builderContext.getQueryParams()
                        .stream().filter(p -> p.getName().equals(OAuthConstants.REDIRECT_URI))
                        .findFirst();
                if (parameter.isPresent()) {
                    builder.addParameter(parameter.get().getName(), parameter.get().getValue());
                }

                parameter = builderContext.getQueryParams()
                        .stream().filter(p -> p.getName().equals(OAuthConstants.ACR_VALUES))
                        .findFirst();
                if (parameter.isPresent()) {
                    builder.addParameter(parameter.get().getName(), parameter.get().getValue());
                }
                return builder.build().toString();
            }
            return url;
        };
    }

    @ConditionalOnMissingBean(name = "requiresAuthenticationAuthorizeInterceptor")
    @Bean
    public SecurityInterceptor requiresAuthenticationAuthorizeInterceptor() {
        return new SecurityInterceptor(oauthSecConfig(), Authenticators.CAS_OAUTH_CLIENT);
    }

    @ConditionalOnMissingBean(name = "consentApprovalViewResolver")
    @Bean
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OAuth20ConsentApprovalViewResolver();
    }

    @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OAuth20CallbackAuthorizeViewResolver() {
        };
    }

    @ConditionalOnMissingBean(name = "requiresAuthenticationAccessTokenInterceptor")
    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor() {
        final String clients = Stream.of(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
                        Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
                        Authenticators.CAS_OAUTH_CLIENT_USER_FORM).collect(Collectors.joining(","));
        return new SecurityInterceptor(oauthSecConfig(), clients);
    }

    @ConditionalOnMissingBean(name = "oauthInterceptor")
    @Bean
    public HandlerInterceptorAdapter oauthInterceptor() {
        return new OAuth20HandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor(), requiresAuthenticationAuthorizeInterceptor());
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oauthInterceptor()).addPathPatterns(BASE_OAUTH20_URL.concat("/").concat("*"));
    }

    @Bean
    public OAuthCasClientRedirectActionBuilder defaultOAuthCasClientRedirectActionBuilder() {
        return new DefaultOAuthCasClientRedirectActionBuilder();
    }

    @ConditionalOnMissingBean(name = "oAuthClientAuthenticator")
    @Bean
    public Authenticator<UsernamePasswordCredentials> oAuthClientAuthenticator() {
        return new OAuthClientAuthenticator(oAuthValidator(), this.servicesManager);
    }

    @ConditionalOnMissingBean(name = "oAuthUserAuthenticator")
    @Bean
    public Authenticator<UsernamePasswordCredentials> oAuthUserAuthenticator() {
        return new OAuthUserAuthenticator(authenticationSystemSupport);
    }

    @ConditionalOnMissingBean(name = "oAuthValidator")
    @Bean
    public OAuth20Validator oAuthValidator() {
        return new OAuth20Validator(webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "oauthAccessTokenResponseGenerator")
    @Bean
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
        return new OAuthAccessTokenExpirationPolicy(
                casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds(),
                casProperties.getAuthn().getOauth().getAccessToken().getTimeToKillInSeconds()
        );
    }

    private ExpirationPolicy oAuthCodeExpirationPolicy() {
        return new OAuthCodeExpirationPolicy(casProperties.getAuthn().getOauth().getCode().getNumberOfUses(),
                casProperties.getAuthn().getOauth().getCode().getTimeToKillInSeconds());
    }

    @Bean
    public UniqueTicketIdGenerator oAuthCodeIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    public UniqueTicketIdGenerator refreshTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultOAuthCodeFactory")
    public OAuthCodeFactory defaultOAuthCodeFactory() {
        return new DefaultOAuthCodeFactory(oAuthCodeIdGenerator(), oAuthCodeExpirationPolicy());
    }

    @Bean
    @ConditionalOnMissingBean(name = "callbackAuthorizeController")
    public OAuth20CallbackAuthorizeController callbackAuthorizeController() {
        return new OAuth20CallbackAuthorizeController(servicesManager, ticketRegistry,
                oAuthValidator(), defaultAccessTokenFactory(), oauthPrincipalFactory(), webApplicationServiceFactory,
                oauthSecConfig(), callbackController(), callbackAuthorizeViewResolver());
    }

    @ConditionalOnMissingBean(name = "accessTokenController")
    @Bean
    public OAuth20AccessTokenController accessTokenController() {
        return new OAuth20AccessTokenController(
                servicesManager,
                ticketRegistry,
                oAuthValidator(), defaultAccessTokenFactory(),
                oauthPrincipalFactory(),
                webApplicationServiceFactory,
                defaultRefreshTokenFactory(), accessTokenResponseGenerator()
        );
    }

    @ConditionalOnMissingBean(name = "profileController")
    @Bean
    public OAuth20ProfileController profileController() {
        return new OAuth20ProfileController(servicesManager,
                ticketRegistry, oAuthValidator(), defaultAccessTokenFactory(),
                oauthPrincipalFactory(), webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "authorizeController")
    @Bean
    public OAuth20AuthorizeController authorizeController() {
        return new OAuth20AuthorizeController(
                servicesManager, ticketRegistry, oAuthValidator(), defaultAccessTokenFactory(),
                oauthPrincipalFactory(), webApplicationServiceFactory, defaultOAuthCodeFactory(),
                consentApprovalViewResolver()
        );
    }

    @Bean
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

    @Bean
    @ConditionalOnMissingBean(name = "oauth20AuthenticationRequestServiceSelectionStrategy")
    public AuthenticationRequestServiceSelectionStrategy oauth20AuthenticationRequestServiceSelectionStrategy() {
        return new OAuth20AuthenticationRequestServiceSelectionStrategy(servicesManager, webApplicationServiceFactory);
    }

    @Bean
    public CallbackController callbackController() {
        final CallbackController c = new CallbackController();
        c.setConfig(oauthSecConfig());
        return c;
    }


    @Bean
    public UniqueTicketIdGenerator accessTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @PostConstruct
    public void initializeServletApplicationContext() {
        final String oAuthCallbackUrl = casProperties.getServer().getPrefix() + BASE_OAUTH20_URL + '/'
                + OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION;

        final Service callbackService = this.webApplicationServiceFactory.createService(oAuthCallbackUrl);
        final RegisteredService svc = servicesManager.findServiceBy(callbackService);

        if (svc == null || !svc.getServiceId().equals(oAuthCallbackUrl)) {
            final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
            service.setName("OAuth Callback url");
            service.setDescription("OAuth Wrapper Callback Url");
            service.setServiceId(oAuthCallbackUrl);
            service.setEvaluationOrder(Integer.MIN_VALUE);
            service.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

            servicesManager.save(service);
            servicesManager.load();
        }

        this.authenticationRequestServiceSelectionStrategies.add(0, oauth20AuthenticationRequestServiceSelectionStrategy());
    }
}
