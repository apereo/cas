package org.apereo.cas.oidc.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.web.OidcHandlerInterceptorAdapter;
import org.apereo.cas.oidc.web.OidcLocaleChangeInterceptor;
import org.apereo.cas.oidc.web.controllers.authorize.OidcAuthorizeEndpointController;
import org.apereo.cas.oidc.web.controllers.authorize.OidcPushedAuthorizeEndpointController;
import org.apereo.cas.oidc.web.controllers.discovery.OidcWellKnownEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientConfigurationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcInitialAccessTokenController;
import org.apereo.cas.oidc.web.controllers.introspection.OidcIntrospectionEndpointController;
import org.apereo.cas.oidc.web.controllers.jwks.OidcJwksEndpointController;
import org.apereo.cas.oidc.web.controllers.jwks.OidcJwksRotationEndpoint;
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
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.CasProtocolViewFactory;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.authorization.authorizer.DefaultAuthorizers;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.matching.matcher.DefaultMatchers;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OidcEndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@AutoConfiguration
public class OidcEndpointsConfiguration {

    @Configuration(value = "OidcEndpointsMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsMultifactorAuthenticationConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcMultifactorAuthenticationTrigger")
        public MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger(
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final OAuth20RequestParameterResolver oauthRequestParameterResolver,
            @Qualifier(MultifactorAuthenticationProviderResolver.BEAN_NAME)
            final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver,
            @Qualifier(OidcServerDiscoverySettings.BEAN_NAME_FACTORY)
            final FactoryBean<OidcServerDiscoverySettings> oidcServerDiscoverySettingsFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new OidcMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver,
                applicationContext, oauthRequestParameterResolver, oidcServerDiscoverySettingsFactory);
        }

    }

    @Configuration(value = "OidcEndpointsLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsLogoutConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = OidcPostLogoutRedirectUrlMatcher.BEAN_NAME_POST_LOGOUT_REDIRECT_URL_MATCHER)
        public OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher() {
            return String::equalsIgnoreCase;
        }

    }

    @Configuration(value = "OidcInterceptorsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcInterceptorsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor requiresAuthenticationDynamicRegistrationInterceptor(
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig) {
            val interceptor = new SecurityInterceptor(oauthSecConfig,
                Authenticators.CAS_OAUTH_CLIENT_DYNAMIC_REGISTRATION_AUTHN,
                DefaultAuthorizers.IS_FULLY_AUTHENTICATED, DefaultMatchers.SECURITYHEADERS);
            return interceptor;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor requiresAuthenticationClientConfigurationInterceptor(
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig) {
            val clients = String.join(",", OidcConstants.CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN);
            val interceptor = new SecurityInterceptor(oauthSecConfig, clients, DefaultAuthorizers.IS_FULLY_AUTHENTICATED,
                    DefaultMatchers.SECURITYHEADERS);
            return interceptor;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor oauthInterceptor(
            final ObjectProvider<List<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors,
            final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthRequestValidators,
            @Qualifier("oauthDistributedSessionStore")
            final ObjectProvider<SessionStore> oauthDistributedSessionStore,
            @Qualifier("requiresAuthenticationAuthorizeInterceptor")
            final ObjectProvider<HandlerInterceptor> requiresAuthenticationAuthorizeInterceptor,
            @Qualifier("requiresAuthenticationAccessTokenInterceptor")
            final ObjectProvider<HandlerInterceptor> requiresAuthenticationAccessTokenInterceptor,
            @Qualifier("requiresAuthenticationClientConfigurationInterceptor")
            final ObjectProvider<HandlerInterceptor> requiresAuthenticationClientConfigurationInterceptor,
            @Qualifier("requiresAuthenticationDynamicRegistrationInterceptor")
            final ObjectProvider<HandlerInterceptor> requiresAuthenticationDynamicRegistrationInterceptor,
            @Qualifier(OAuth20RequestParameterResolver.BEAN_NAME)
            final ObjectProvider<OAuth20RequestParameterResolver> oauthRequestParameterResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            final CasConfigurationProperties casProperties) {

            return new OidcHandlerInterceptorAdapter(
                requiresAuthenticationAccessTokenInterceptor,
                requiresAuthenticationAuthorizeInterceptor,
                requiresAuthenticationDynamicRegistrationInterceptor,
                requiresAuthenticationClientConfigurationInterceptor,
                casProperties,
                accessTokenGrantRequestExtractors,
                servicesManager,
                oauthDistributedSessionStore,
                oauthRequestValidators,
                oauthRequestParameterResolver);
        }
    }

    @Configuration(value = "OidcEndpointsWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Slf4j
    public static class OidcEndpointsWebConfiguration {
        private static String getOidcBaseEndpoint(final OidcIssuerService issuerService,
                                                  final CasConfigurationProperties casProperties) {
            val issuer = issuerService.determineIssuer(Optional.empty());
            val endpoint = StringUtils.remove(issuer, casProperties.getServer().getPrefix());
            return StringUtils.prependIfMissing(endpoint, "/");
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WebMvcConfigurer oidcWebMvcConfigurer(
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oauthInterceptor")
            final ObjectProvider<HandlerInterceptor> oauthInterceptor,
            final CasConfigurationProperties casProperties) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(
                    @Nonnull
                    final InterceptorRegistry registry) {
                    val baseEndpoint = getOidcBaseEndpoint(oidcIssuerService, casProperties);
                    LOGGER.info("Registering CAS OpenID Connect endpoints under [{}]. Verify to make sure this value "
                        + "is correctly defined based on your issuer and server settings, defined in CAS configuration. "
                        + "Failure to specify the correct value may interfere with OpenID Connect functionality.", baseEndpoint);
                    registry.addInterceptor(new RefreshableHandlerInterceptor(oauthInterceptor))
                        .order(100)
                        .addPathPatterns(baseEndpoint.concat("/*"));
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcProtocolEndpointConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ProtocolEndpointWebSecurityConfigurer<Void> oidcProtocolEndpointConfigurer(
            @Qualifier(OidcIssuerService.BEAN_NAME)
            final OidcIssuerService oidcIssuerService,
            final CasConfigurationProperties casProperties) {
            val baseEndpoint = getOidcBaseEndpoint(oidcIssuerService, casProperties);
            return new ProtocolEndpointWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(baseEndpoint);
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcLocaleChangeInterceptor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor oidcLocaleChangeInterceptor(
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ObjectProvider<ArgumentExtractor> argumentExtractor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            final ObjectProvider<CasConfigurationProperties> casProperties) {
            val interceptor = new OidcLocaleChangeInterceptor(casProperties,
                argumentExtractor, servicesManager);
            interceptor.setParamName(OidcConstants.UI_LOCALES);
            return interceptor;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcConfirmView")
        public View oidcConfirmView(final ConfigurableApplicationContext applicationContext,
                                    @Qualifier(CasProtocolViewFactory.BEAN_NAME_THYMELEAF_VIEW_FACTORY)
                                    final CasProtocolViewFactory casProtocolViewFactory) {
            return casProtocolViewFactory.create(applicationContext, "protocol/oidc/confirm");
        }
    }

    @Configuration(value = "OidcControllerEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    public static class OidcControllerEndpointsConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcWellKnownController")
        @Bean
        public OidcWellKnownEndpointController oidcWellKnownController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext,
            @Qualifier("oidcWebFingerDiscoveryService")
            final OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService) {
            return new OidcWellKnownEndpointController(oidcConfigurationContext, oidcWebFingerDiscoveryService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcProfileController")
        @Bean
        public OidcUserProfileEndpointController oidcProfileController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcUserProfileEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public OidcAuthorizeEndpointController oidcAuthorizeController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcAuthorizeEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public OidcPushedAuthorizeEndpointController oidcPushedAuthorizeController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcPushedAuthorizeEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcLogoutEndpointController")
        public OidcLogoutEndpointController oidcLogoutEndpointController(
            @Qualifier(OidcPostLogoutRedirectUrlMatcher.BEAN_NAME_POST_LOGOUT_REDIRECT_URL_MATCHER)
            final OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher,
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcLogoutEndpointController(oidcConfigurationContext,
                postLogoutRedirectUrlMatcher, urlValidator);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcRevocationEndpointController")
        public OidcRevocationEndpointController oidcRevocationEndpointController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcRevocationEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcAccessTokenController")
        public OidcAccessTokenEndpointController oidcAccessTokenController(
            @Qualifier("accessTokenGrantAuditableRequestExtractor")
            final AuditableExecution accessTokenGrantAuditableRequestExtractor,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcAccessTokenEndpointController(oidcConfigurationContext,
                accessTokenGrantAuditableRequestExtractor);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcDynamicClientRegistrationEndpointController")
        public OidcDynamicClientRegistrationEndpointController oidcDynamicClientRegistrationEndpointController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcDynamicClientRegistrationEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcClientConfigurationEndpointController")
        @Bean
        public OidcClientConfigurationEndpointController oidcClientConfigurationEndpointController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcClientConfigurationEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcInitialAccessTokenController")
        @Bean
        public OidcInitialAccessTokenController oidcInitialAccessTokenController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcInitialAccessTokenController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJwksController")
        @Bean
        public OidcJwksEndpointController oidcJwksController(
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService,
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcJwksEndpointController(oidcConfigurationContext, oidcJsonWebKeystoreGeneratorService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcIntrospectionEndpointController")
        public OidcIntrospectionEndpointController oidcIntrospectionEndpointController(
            @Qualifier(OidcConfigurationContext.BEAN_NAME)
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcIntrospectionEndpointController(oidcConfigurationContext);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcJwksRotationEndpoint jwksRotationEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final ObjectProvider<OidcJsonWebKeystoreRotationService> oidcJsonWebKeystoreRotationService) {
            return new OidcJwksRotationEndpoint(casProperties, oidcJsonWebKeystoreRotationService);
        }
    }

    @Configuration(value = "OidcEndpointsWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsWebflowConfiguration {

        @ConditionalOnMissingBean(name = "oidcCasWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer oidcCasWebflowExecutionPlanConfigurer(
            @Qualifier("oidcWebflowConfigurer")
            final CasWebflowConfigurer oidcWebflowConfigurer,
            @Qualifier("oidcLocaleChangeInterceptor")
            final HandlerInterceptor oidcLocaleChangeInterceptor,
            @Qualifier("oidcCasWebflowLoginContextProvider")
            final CasWebflowLoginContextProvider oidcCasWebflowLoginContextProvider) {
            return plan -> {
                plan.registerWebflowConfigurer(oidcWebflowConfigurer);
                plan.registerWebflowInterceptor(oidcLocaleChangeInterceptor);
                plan.registerWebflowLoginContextProvider(oidcCasWebflowLoginContextProvider);
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcCasWebflowLoginContextProvider")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowLoginContextProvider oidcCasWebflowLoginContextProvider(
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor) {
            return new OidcCasWebflowLoginContextProvider(argumentExtractor);
        }


        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowEventResolver oidcAuthenticationContextWebflowEventResolver(
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
            @Qualifier("oidcMultifactorAuthenticationTrigger")
            final MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger) {
            val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
                casWebflowConfigurationContext, oidcMultifactorAuthenticationTrigger);
            initialAuthenticationAttemptWebflowEventResolver.addDelegate(r);
            return r;
        }

        @ConditionalOnMissingBean(name = "oidcWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer oidcWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val cfg = new OidcWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
            cfg.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
            return cfg;
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_OIDC_REGSTERED_SERVICE_UI)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action oidcRegisteredServiceUIAction(
            @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
            final AuthenticationServiceSelectionStrategy oauth20AuthenticationServiceSelectionStrategy,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new OidcRegisteredServiceUIAction(servicesManager, oauth20AuthenticationServiceSelectionStrategy);
        }
    }
}
