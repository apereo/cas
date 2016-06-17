package org.apereo.cas.config;

import org.apereo.cas.OAuthApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.authenticator.OAuthClientAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuthUserAuthenticator;
import org.apereo.cas.support.oauth.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.support.oauth.ticket.accesstoken.DefaultAccessTokenFactory;
import org.apereo.cas.support.oauth.ticket.accesstoken.OAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.support.oauth.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.support.oauth.ticket.code.OAuthCodeFactory;
import org.apereo.cas.support.oauth.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.support.oauth.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.support.oauth.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.support.oauth.validator.OAuth20ValidationServiceSelectionStrategy;
import org.apereo.cas.support.oauth.validator.OAuthValidator;
import org.apereo.cas.support.oauth.web.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenController;
import org.apereo.cas.support.oauth.web.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.OAuth20AuthorizeController;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeController;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20ProfileController;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.jasig.cas.client.util.URIBuilder;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.CallbackUrlResolver;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.pac4j.springframework.web.CallbackController;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * This this {@link OAuthConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oauthConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OAuthConfiguration extends WebMvcConfigurerAdapter {

    private static final String CAS_OAUTH_CLIENT = "CasOAuthClient";

    @Autowired
    private CallbackController callbackController;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("validationServiceSelectionStrategies")
    private List validationServiceSelectionStrategies;

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
    @Bean(autowire = Autowire.BY_NAME)
    public AccessTokenResponseGenerator accessTokenResponseGenerator() {
        return new OAuth20AccessTokenResponseGenerator();
    }

    @ConditionalOnMissingBean(name = "oauthCasClientRedirectActionBuilder")
    @Bean(autowire = Autowire.BY_NAME)
    public OAuthCasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new DefaultOAuthCasClientRedirectActionBuilder();
    }

    @Bean
    public Config oauthSecConfig() {
        final CasClient oauthCasClient = new CasClient(casProperties.getServer().getLoginUrl()) {
            @Override
            protected RedirectAction retrieveRedirectAction(final WebContext context) {
                return oauthCasClientRedirectActionBuilder().build(this, context);
            }
        };

        oauthCasClient.setName(CAS_OAUTH_CLIENT);
        oauthCasClient.setCallbackUrlResolver(buildOAuthCasCallbackUrlResolver());

        final DirectBasicAuthClient basicAuthClient = new DirectBasicAuthClient(oAuthClientAuthenticator());
        basicAuthClient.setName("clientBasicAuth");

        final DirectFormClient directFormClient = new DirectFormClient(oAuthClientAuthenticator());
        directFormClient.setName("clientForm");
        directFormClient.setUsernameParameter(OAuthConstants.CLIENT_ID);
        directFormClient.setPasswordParameter(OAuthConstants.CLIENT_SECRET);

        final DirectFormClient userFormClient = new DirectFormClient(oAuthUserAuthenticator());
        userFormClient.setName("userForm");

        final String callbackUrl = casProperties.getServer().getPrefix().concat("/oauth2.0/callbackAuthorize");
        return new Config(callbackUrl, oauthCasClient, basicAuthClient, directFormClient, userFormClient);
    }

    private CallbackUrlResolver buildOAuthCasCallbackUrlResolver() {
        return (url, context) -> {
            final String callbackUrl = casProperties.getServer().getPrefix().concat("/oauth2.0/callbackAuthorize");
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
                return builder.build().toString();
            }
            return url;
        };
    }

    /**
     * Requires authentication authorize interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @ConditionalOnMissingBean(name = "requiresAuthenticationAuthorizeInterceptor")
    @Bean
    public RequiresAuthenticationInterceptor requiresAuthenticationAuthorizeInterceptor() {
        return new RequiresAuthenticationInterceptor(oauthSecConfig(), CAS_OAUTH_CLIENT);
    }

    /**
     * Consent approval view resolver.
     *
     * @return the consent approval view resolver
     */
    @ConditionalOnMissingBean(name = "consentApprovalViewResolver")
    @Bean
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OAuth20ConsentApprovalViewResolver();
    }

    /**
     * Callback authorize view resolver.
     *
     * @return the oauth 20 callback authorize view resolver
     */
    @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OAuth20CallbackAuthorizeViewResolver() {
        };
    }

    /**
     * Requires authentication access token interceptor requires authentication interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean
    public RequiresAuthenticationInterceptor requiresAuthenticationAccessTokenInterceptor() {
        return new RequiresAuthenticationInterceptor(oauthSecConfig(), "clientBasicAuth,clientForm,userForm");
    }


    /**
     * interceptor handler interceptor adapter.
     *
     * @return the handler interceptor adapter
     */
    @Bean
    public HandlerInterceptorAdapter oauthInterceptor() {
        return new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                                     final Object handler) throws Exception {
                final String requestPath = request.getRequestURI();
                Pattern pattern = Pattern.compile('/' + OAuthConstants.ACCESS_TOKEN_URL + "(/)*$");

                if (pattern.matcher(requestPath).find()) {
                    return requiresAuthenticationAccessTokenInterceptor().preHandle(request, response, handler);
                }

                pattern = Pattern.compile('/' + OAuthConstants.AUTHORIZE_URL + "(/)*$");
                if (pattern.matcher(requestPath).find()) {
                    return requiresAuthenticationAuthorizeInterceptor().preHandle(request, response, handler);
                }
                return true;

            }
        };
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oauthInterceptor())
                .addPathPatterns(OAuthConstants.BASE_OAUTH20_URL.concat("/").concat("*"));
    }

    @Bean
    @RefreshScope
    public BaseApplicationContextWrapper oauthApplicationContextWrapper() {
        final OAuthApplicationContextWrapper w = new OAuthApplicationContextWrapper();
        w.setOauth20ValidationServiceSelectionStrategy(oauth20ValidationServiceSelectionStrategy());
        w.setValidationServiceSelectionStrategies(validationServiceSelectionStrategies);
        w.setWebApplicationServiceFactory(webApplicationServiceFactory);
        return w;
    }

    @Bean
    public OAuthCasClientRedirectActionBuilder defaultOAuthCasClientRedirectActionBuilder() {
        return new DefaultOAuthCasClientRedirectActionBuilder();
    }

    @Bean
    public UsernamePasswordAuthenticator oAuthClientAuthenticator() {
        final OAuthClientAuthenticator c = new OAuthClientAuthenticator();
        c.setValidator(oAuthValidator());
        c.setServicesManager(this.servicesManager);
        return c;
    }

    @Bean
    public UsernamePasswordAuthenticator oAuthUserAuthenticator() {
        final OAuthUserAuthenticator w = new OAuthUserAuthenticator();
        w.setAuthenticationSystemSupport(authenticationSystemSupport);
        return w;
    }

    @Bean
    public OAuthValidator oAuthValidator() {
        return new OAuthValidator();
    }

    @Bean
    public AccessTokenResponseGenerator oauthAccessTokenResponseGenerator() {
        return new OAuth20AccessTokenResponseGenerator();
    }

    @Bean
    public AccessTokenFactory defaultAccessTokenFactory() {
        final DefaultAccessTokenFactory f = new DefaultAccessTokenFactory();
        f.setAccessTokenIdGenerator(accessTokenIdGenerator());
        f.setExpirationPolicy(accessTokenExpirationPolicy());
        return f;
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy accessTokenExpirationPolicy() {
        return new OAuthAccessTokenExpirationPolicy(
                casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds(),
                casProperties.getAuthn().getOauth().getAccessToken().getTimeToKillInSeconds(),
                TimeUnit.SECONDS
        );
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy oAuthCodeExpirationPolicy() {
        return new OAuthCodeExpirationPolicy(casProperties.getAuthn().getOauth().getCode().getNumberOfUses(),
                TimeUnit.SECONDS.toMillis(casProperties.getAuthn().getOauth().getCode().getTimeToKillInSeconds()));
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
    public OAuthCodeFactory defaultOAuthCodeFactory() {
        final DefaultOAuthCodeFactory f = new DefaultOAuthCodeFactory();
        f.setExpirationPolicy(oAuthCodeExpirationPolicy());
        f.setoAuthCodeIdGenerator(oAuthCodeIdGenerator());
        return f;
    }

    @Bean
    public OAuth20CallbackAuthorizeController callbackAuthorizeController() {
        final OAuth20CallbackAuthorizeController c = new OAuth20CallbackAuthorizeController();
        c.setCallbackController(this.callbackController);
        c.setConfig(oauthSecConfig());
        c.setAuth20CallbackAuthorizeViewResolver(callbackAuthorizeViewResolver());
        return c;
    }

    @Bean
    public OAuth20AccessTokenController accessTokenController() {
        final OAuth20AccessTokenController c = new OAuth20AccessTokenController();
        c.setAccessTokenFactory(defaultAccessTokenFactory());
        c.setAccessTokenResponseGenerator(accessTokenResponseGenerator());
        c.setPrincipalFactory(oauthPrincipalFactory());
        c.setRefreshTokenFactory(defaultRefreshTokenFactory());
        c.setServicesManager(servicesManager);
        c.setTicketRegistry(ticketRegistry);
        c.setValidator(oAuthValidator());
        return c;
    }

    @Bean
    public OAuth20ProfileController profileController() {
        final OAuth20ProfileController c = new OAuth20ProfileController();
        c.setAccessTokenFactory(defaultAccessTokenFactory());
        c.setPrincipalFactory(oauthPrincipalFactory());
        c.setServicesManager(servicesManager);
        c.setTicketRegistry(ticketRegistry);
        c.setValidator(oAuthValidator());
        return c;
    }

    @Bean
    public OAuth20AuthorizeController authorizeController() {
        final OAuth20AuthorizeController c = new OAuth20AuthorizeController();
        c.setAccessTokenFactory(defaultAccessTokenFactory());
        c.setPrincipalFactory(oauthPrincipalFactory());
        c.setServicesManager(servicesManager);
        c.setTicketRegistry(ticketRegistry);
        c.setValidator(oAuthValidator());
        c.setConsentApprovalViewResolver(consentApprovalViewResolver());
        c.setoAuthCodeFactory(defaultOAuthCodeFactory());
        return c;
    }

    @Bean
    public PrincipalFactory oauthPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public RefreshTokenFactory defaultRefreshTokenFactory() {
        final DefaultRefreshTokenFactory f = new DefaultRefreshTokenFactory();
        f.setExpirationPolicy(refreshTokenExpirationPolicy());
        f.setRefreshTokenIdGenerator(refreshTokenIdGenerator());
        return f;
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy refreshTokenExpirationPolicy() {
        return new OAuthRefreshTokenExpirationPolicy(
                TimeUnit.SECONDS.toMillis(casProperties.getAuthn().getOauth().getRefreshToken().getTimeToKillInSeconds())
        );
    }

    @Bean
    public ValidationServiceSelectionStrategy oauth20ValidationServiceSelectionStrategy() {
        final OAuth20ValidationServiceSelectionStrategy s = new OAuth20ValidationServiceSelectionStrategy();
        s.setServicesManager(servicesManager);
        s.setWebApplicationServiceFactory(webApplicationServiceFactory);
        return s;
    }

    @Bean
    public UniqueTicketIdGenerator accessTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }
}
