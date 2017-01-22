package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.OidcCasClientRedirectActionBuilder;
import org.apereo.cas.OidcClientRegistrationRequest;
import org.apereo.cas.OidcClientRegistrationRequestSerializer;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthCasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.OidcAccessTokenResponseGenerator;
import org.apereo.cas.web.OidcCallbackAuthorizeViewResolver;
import org.apereo.cas.web.OidcConsentApprovalViewResolver;
import org.apereo.cas.web.OidcHandlerInterceptorAdapter;
import org.apereo.cas.web.OidcSecurityInterceptor;
import org.apereo.cas.web.controllers.OidcAccessTokenEndpointController;
import org.apereo.cas.web.controllers.OidcAuthorizeEndpointController;
import org.apereo.cas.web.controllers.OidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.web.controllers.OidcJwksEndpointController;
import org.apereo.cas.web.controllers.OidcProfileEndpointController;
import org.apereo.cas.web.controllers.OidcWellKnownEndpointController;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.OidcAuthenticationContextWebflowEventEventResolver;
import org.apereo.cas.web.flow.OidcRegisteredServiceUIAction;
import org.apereo.cas.web.flow.OidcWebflowConfigurer;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.config.Config;
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
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    private HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor;

    @Autowired(required = false)
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector =
            new FirstMultifactorAuthenticationProviderSelector();

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private AuthenticationRequestServiceSelectionStrategy oauth20AuthenticationRequestServiceSelectionStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("oauthSecConfig")
    private Config oauthSecConfig;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory defaultAccessTokenFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    private RefreshTokenFactory defaultRefreshTokenFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("oAuthValidator")
    private OAuth20Validator oAuth20Validator;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private OAuthCodeFactory defaultOAuthCodeFactory;

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oauthInterceptor()).addPathPatterns('/' + OidcConstants.BASE_OIDC_URL.concat("/").concat("*"));
    }

    @Bean
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OidcConsentApprovalViewResolver();
    }

    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OidcCallbackAuthorizeViewResolver(oidcAuthorizationRequestSupport());
    }

    @Bean
    public OAuthCasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcAuthorizationRequestSupport());
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor() {
        final String clients = Stream.of(
                Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
                Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
                Authenticators.CAS_OAUTH_CLIENT_USER_FORM).collect(Collectors.joining(","));
        return new SecurityInterceptor(oauthSecConfig, clients);
    }

    @Bean
    public HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor() {
        final String name = oauthSecConfig.getClients().findClient(CasClient.class).getName();
        return new OidcSecurityInterceptor(oauthSecConfig, name, oidcAuthorizationRequestSupport());
    }

    @Bean
    public OAuthCasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder(oidcAuthorizationRequestSupport());
    }

    @Bean
    @RefreshScope
    public AccessTokenResponseGenerator oidcAccessTokenResponseGenerator() {
        final OidcProperties oidc = casProperties.getAuthn().getOidc();
        return new OidcAccessTokenResponseGenerator(oidc.getIssuer(), oidc.getSkew(), oidc.getJwksFile());
    }

    @Bean
    public OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport() {
        return new OidcAuthorizationRequestSupport(ticketGrantingTicketCookieGenerator, ticketRegistrySupport);
    }

    @ConditionalOnMissingBean(name = "oidcPrincipalFactory")
    @Bean
    public PrincipalFactory oidcPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public OidcAccessTokenEndpointController oidcAccessTokenController() {
        return new OidcAccessTokenEndpointController(
                servicesManager, ticketRegistry, oAuth20Validator, defaultAccessTokenFactory,
                oidcPrincipalFactory(), webApplicationServiceFactory, defaultRefreshTokenFactory, oidcAccessTokenResponseGenerator());
    }

    @Bean
    public StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer() {
        return new OidcClientRegistrationRequestSerializer();
    }

    @RefreshScope
    @Bean
    public OidcDynamicClientRegistrationEndpointController oidcDynamicClientRegistrationEndpointController() {
        return new OidcDynamicClientRegistrationEndpointController(
                servicesManager, ticketRegistry, oAuth20Validator, defaultAccessTokenFactory,
                oidcPrincipalFactory(), webApplicationServiceFactory, clientRegistrationRequestSerializer(),
                new DefaultRandomStringGenerator(), new DefaultRandomStringGenerator());
    }

    @RefreshScope
    @Bean
    public OidcJwksEndpointController oidcJwksController() {
        return new OidcJwksEndpointController(servicesManager, ticketRegistry, oAuth20Validator, defaultAccessTokenFactory,
                oidcPrincipalFactory(), webApplicationServiceFactory, casProperties.getAuthn().getOidc().getJwksFile());
    }

    @RefreshScope
    @Bean
    public OidcWellKnownEndpointController oidcWellKnownController() {
        return new OidcWellKnownEndpointController(servicesManager, ticketRegistry, oAuth20Validator, defaultAccessTokenFactory,
                oidcPrincipalFactory(), webApplicationServiceFactory);
    }

    @RefreshScope
    @Bean
    public OidcProfileEndpointController oidcProfileController() {
        return new OidcProfileEndpointController(servicesManager, ticketRegistry, oAuth20Validator, defaultAccessTokenFactory,
                oidcPrincipalFactory(), webApplicationServiceFactory);
    }

    @RefreshScope
    @Bean
    public OidcAuthorizeEndpointController oidcAuthorizeController() {
        return new OidcAuthorizeEndpointController(servicesManager,
                ticketRegistry, oAuth20Validator, defaultAccessTokenFactory,
                oidcPrincipalFactory(), webApplicationServiceFactory, defaultOAuthCodeFactory,
                consentApprovalViewResolver());
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver oidcAuthenticationContextWebflowEventResolver() {
        return new OidcAuthenticationContextWebflowEventEventResolver(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authenticationRequestServiceSelectionStrategies, multifactorAuthenticationProviderSelector);
    }

    @Bean
    public CasWebflowConfigurer oidcWebflowConfigurer() {
        final OidcWebflowConfigurer cfg = new OidcWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, oidcRegisteredServiceUIAction());
        cfg.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        return cfg;
    }

    @ConditionalOnMissingBean(name = "oidcRegisteredServiceUIAction")
    @Bean
    public Action oidcRegisteredServiceUIAction() {
        return new OidcRegisteredServiceUIAction(this.servicesManager, oauth20AuthenticationRequestServiceSelectionStrategy);
    }

    @Bean
    public HandlerInterceptorAdapter oauthInterceptor() {
        final OidcConstants.DynamicClientRegistrationMode mode =
                OidcConstants.DynamicClientRegistrationMode.valueOf(StringUtils.defaultIfBlank(
                        casProperties.getAuthn().getOidc().getDynamicClientRegistrationMode(),
                        OidcConstants.DynamicClientRegistrationMode.PROTECTED.name()));

        return new OidcHandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor,
                requiresAuthenticationAuthorizeInterceptor(),
                requiresAuthenticationDynamicRegistrationInterceptor(),
                mode);
    }

    @PostConstruct
    public void initOidcConfig() {
        this.initialAuthenticationAttemptWebflowEventResolver.addDelegate(oidcAuthenticationContextWebflowEventResolver());
    }
}
