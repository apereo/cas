package org.apereo.cas.oidc.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcRestfulJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.web.OidcHandlerInterceptorAdapter;
import org.apereo.cas.oidc.web.OidcLocaleChangeInterceptor;
import org.apereo.cas.oidc.web.controllers.authorize.OidcAuthorizeEndpointController;
import org.apereo.cas.oidc.web.controllers.discovery.OidcWellKnownEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientConfigurationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.oidc.web.controllers.introspection.OidcIntrospectionEndpointController;
import org.apereo.cas.oidc.web.controllers.jwks.OidcJwksEndpointController;
import org.apereo.cas.oidc.web.controllers.logout.OidcLogoutEndpointController;
import org.apereo.cas.oidc.web.controllers.logout.OidcPostLogoutRedirectUrlMatcher;
import org.apereo.cas.oidc.web.controllers.profile.OidcUserProfileEndpointController;
import org.apereo.cas.oidc.web.controllers.token.OidcAccessTokenEndpointController;
import org.apereo.cas.oidc.web.controllers.token.OidcRevocationEndpointController;
import org.apereo.cas.oidc.web.flow.OidcCasWebflowLoginContextProvider;
import org.apereo.cas.oidc.web.flow.OidcMultifactorAuthenticationTrigger;
import org.apereo.cas.oidc.web.flow.OidcRegisteredServiceUIAction;
import org.apereo.cas.oidc.web.flow.OidcWebflowConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.validation.CasProtocolViewFactory;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.PublicJsonWebKey;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcEndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcEndpointsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcEndpointsConfiguration {

    @Autowired
    @Qualifier("oauthDistributedSessionStore")
    private ObjectProvider<SessionStore> oauthDistributedSessionStore;

    @Autowired
    @Qualifier("accessTokenGrantAuditableRequestExtractor")
    private ObjectProvider<AuditableExecution> accessTokenGrantAuditableRequestExtractor;

    @Autowired
    @Qualifier("oauthAuthorizationRequestValidators")
    private ObjectProvider<Set<OAuth20AuthorizationRequestValidator>> oauthRequestValidators;

    @Autowired
    @Qualifier("requiresAuthenticationAccessTokenInterceptor")
    private ObjectProvider<HandlerInterceptor> requiresAuthenticationAccessTokenInterceptor;

    @Autowired
    @Qualifier("requiresAuthenticationAuthorizeInterceptor")
    private ObjectProvider<HandlerInterceptor> requiresAuthenticationAuthorizeInterceptor;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> logoutFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
    private ObjectProvider<AuthenticationServiceSelectionStrategy> oauth20AuthenticationServiceSelectionStrategy;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casProtocolViewFactory")
    private ObjectProvider<CasProtocolViewFactory> casProtocolViewFactory;

    @Autowired
    @Qualifier("oidcConfigurationContext")
    private ObjectProvider<OidcConfigurationContext> oidcConfigurationContext;

    @Autowired
    @Qualifier("oauthSecConfig")
    private ObjectProvider<Config> oauthSecConfig;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @Autowired
    @Qualifier("accessTokenGrantRequestExtractors")
    private ObjectProvider<Collection<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors;


    @Autowired
    @Qualifier("multifactorAuthenticationProviderResolver")
    private ObjectProvider<MultifactorAuthenticationProviderResolver> multifactorAuthenticationProviderResolver;

    @Autowired
    @Qualifier("urlValidator")
    private ObjectProvider<UrlValidator> urlValidator;

    @Autowired
    @Qualifier("oidcIssuerService")
    private ObjectProvider<OidcIssuerService> oidcIssuerService;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcMultifactorAuthenticationTrigger")
    public MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger() {
        return new OidcMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver.getObject(), this.applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCache")
    @RefreshScope
    public LoadingCache<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCache() {
        val oidc = casProperties.getAuthn().getOidc();
        return Caffeine.newBuilder().maximumSize(1)
            .expireAfterWrite(Duration.ofMinutes(oidc.getJwks().getJwksCacheInMinutes()))
            .build(oidcDefaultJsonWebKeystoreCacheLoader());
    }

    @Bean
    public WebMvcConfigurer oidcWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                val baseEndpoint = getOidcBaseEndpoint(oidcIssuerService.getObject());
                registry.addInterceptor(oauthInterceptor())
                    .order(100)
                    .addPathPatterns(baseEndpoint.concat("/*"));
            }
        };
    }

    @Bean
    public HandlerInterceptor requiresAuthenticationDynamicRegistrationInterceptor() {
        val clients = String.join(",",
            Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
            Authenticators.CAS_OAUTH_CLIENT_ACCESS_TOKEN_AUTHN,
            Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
            Authenticators.CAS_OAUTH_CLIENT_USER_FORM);
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(), clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        return interceptor;
    }

    @Bean
    public HandlerInterceptor requiresAuthenticationClientConfigurationInterceptor() {
        val clients = String.join(",", OidcConstants.CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN);
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(), clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
        interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
        return interceptor;
    }

    @Bean
    public HandlerInterceptor oauthInterceptor() {
        val oidc = casProperties.getAuthn().getOidc();
        val mode = OidcConstants.DynamicClientRegistrationMode.valueOf(StringUtils.defaultIfBlank(
            oidc.getCore().getDynamicClientRegistrationMode(),
            OidcConstants.DynamicClientRegistrationMode.PROTECTED.name()));

        return new OidcHandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor.getObject(),
            requiresAuthenticationAuthorizeInterceptor.getObject(),
            requiresAuthenticationDynamicRegistrationInterceptor(),
            requiresAuthenticationClientConfigurationInterceptor(),
            mode,
            accessTokenGrantRequestExtractors.getObject(),
            servicesManager.getObject(),
            oauthDistributedSessionStore.getObject(),
            oauthRequestValidators.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcProtocolEndpointConfigurer")
    @RefreshScope
    public ProtocolEndpointWebSecurityConfigurer<Void> oidcProtocolEndpointConfigurer() {
        val baseEndpoint = getOidcBaseEndpoint(oidcIssuerService.getObject());
        return new ProtocolEndpointWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of(baseEndpoint);
            }
        };
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "oidcIntrospectionEndpointController")
    public OidcIntrospectionEndpointController oidcIntrospectionEndpointController() {
        return new OidcIntrospectionEndpointController(oidcConfigurationContext.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = OidcPostLogoutRedirectUrlMatcher.BEAN_NAME_POST_LOGOUT_REDIRECT_URL_MATCHER)
    public OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher() {
        return String::equalsIgnoreCase;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "oidcLogoutEndpointController")
    public OidcLogoutEndpointController oidcLogoutEndpointController() {
        return new OidcLogoutEndpointController(oidcConfigurationContext.getObject(),
            postLogoutRedirectUrlMatcher(), urlValidator.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "oidcRevocationEndpointController")
    public OidcRevocationEndpointController oidcRevocationEndpointController() {
        return new OidcRevocationEndpointController(oidcConfigurationContext.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "oidcAccessTokenController")
    public OidcAccessTokenEndpointController oidcAccessTokenController() {
        return new OidcAccessTokenEndpointController(oidcConfigurationContext.getObject(),
            accessTokenGrantAuditableRequestExtractor.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "oidcDynamicClientRegistrationEndpointController")
    public OidcDynamicClientRegistrationEndpointController oidcDynamicClientRegistrationEndpointController() {
        return new OidcDynamicClientRegistrationEndpointController(oidcConfigurationContext.getObject());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcClientConfigurationEndpointController")
    @Bean
    public OidcClientConfigurationEndpointController oidcClientConfigurationEndpointController() {
        return new OidcClientConfigurationEndpointController(oidcConfigurationContext.getObject());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcJwksController")
    @Bean
    public OidcJwksEndpointController oidcJwksController() {
        return new OidcJwksEndpointController(oidcConfigurationContext.getObject(), oidcJsonWebKeystoreGeneratorService());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCacheLoader")
    @RefreshScope
    public CacheLoader<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCacheLoader() {
        return new OidcDefaultJsonWebKeystoreCacheLoader(oidcJsonWebKeystoreGeneratorService());
    }

    @Bean(initMethod = "generate")
    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreGeneratorService")
    public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService() {
        val oidc = casProperties.getAuthn().getOidc();
        if (StringUtils.isNotBlank(oidc.getJwks().getRest().getUrl())) {
            return new OidcRestfulJsonWebKeystoreGeneratorService(oidc);
        }
        return new OidcDefaultJsonWebKeystoreGeneratorService(oidc);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcWellKnownController")
    @Bean
    @Autowired
    public OidcWellKnownEndpointController oidcWellKnownController(@Qualifier("oidcWebFingerDiscoveryService") final OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService) {
        return new OidcWellKnownEndpointController(oidcConfigurationContext.getObject(), oidcWebFingerDiscoveryService);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "oidcProfileController")
    @Bean
    public OidcUserProfileEndpointController oidcProfileController() {
        return new OidcUserProfileEndpointController(oidcConfigurationContext.getObject());
    }

    @RefreshScope
    @Bean
    public OidcAuthorizeEndpointController oidcAuthorizeController() {
        return new OidcAuthorizeEndpointController(oidcConfigurationContext.getObject());
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver oidcAuthenticationContextWebflowEventResolver() {
        val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(), oidcMultifactorAuthenticationTrigger());
        Objects.requireNonNull(this.initialAuthenticationAttemptWebflowEventResolver.getObject()).addDelegate(r);
        return r;
    }

    @ConditionalOnMissingBean(name = "oidcWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer oidcWebflowConfigurer() {
        val cfg = new OidcWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), oidcRegisteredServiceUIAction(), applicationContext, casProperties);
        cfg.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry.getObject());
        return cfg;
    }

    @ConditionalOnMissingBean(name = "oidcRegisteredServiceUIAction")
    @Bean
    @RefreshScope
    public Action oidcRegisteredServiceUIAction() {
        return new OidcRegisteredServiceUIAction(this.servicesManager.getObject(), oauth20AuthenticationServiceSelectionStrategy.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcLocaleChangeInterceptor")
    @RefreshScope
    public HandlerInterceptor oidcLocaleChangeInterceptor() {
        val interceptor = new OidcLocaleChangeInterceptor(
            casProperties.getLocale(), argumentExtractor.getObject(), servicesManager.getObject());
        interceptor.setParamName(OidcConstants.UI_LOCALES);
        return interceptor;
    }

    @ConditionalOnMissingBean(name = "oidcCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer oidcCasWebflowExecutionPlanConfigurer() {
        return plan -> {
            plan.registerWebflowConfigurer(oidcWebflowConfigurer());
            plan.registerWebflowInterceptor(oidcLocaleChangeInterceptor());
            plan.registerWebflowLoginContextProvider(oidcCasWebflowLoginContextProvider());
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcConfirmView")
    public View oidcConfirmView() {
        return casProtocolViewFactory.getObject().create(applicationContext, "protocol/oidc/confirm");
    }

    @Bean
    @ConditionalOnMissingBean(name = "oidcCasWebflowLoginContextProvider")
    @RefreshScope
    public CasWebflowLoginContextProvider oidcCasWebflowLoginContextProvider() {
        return new OidcCasWebflowLoginContextProvider(argumentExtractor.getObject());
    }

    private String getOidcBaseEndpoint(final OidcIssuerService issuerService) {
        val issuer = issuerService.determineIssuer(Optional.empty());
        val endpoint = StringUtils.remove(issuer, casProperties.getServer().getPrefix());
        return StringUtils.prependIfMissing(endpoint, "/");
    }
}
