package org.apereo.cas.oidc.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreListener;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcGroovyJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcRestfulJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcDefaultJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.web.OidcHandlerInterceptorAdapter;
import org.apereo.cas.oidc.web.OidcLocaleChangeInterceptor;
import org.apereo.cas.oidc.web.controllers.authorize.OidcAuthorizeEndpointController;
import org.apereo.cas.oidc.web.controllers.discovery.OidcWellKnownEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientConfigurationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDynamicClientRegistrationEndpointController;
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
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.util.spring.CasEventListener;
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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.List;
import java.util.Optional;

/**
 * This is {@link OidcEndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "OidcEndpointsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcEndpointsConfiguration {

    @Configuration(value = "OidcEndpointsMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsMultifactorAuthenticationConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcMultifactorAuthenticationTrigger")
        public MultifactorAuthenticationTrigger oidcMultifactorAuthenticationTrigger(
            @Qualifier("multifactorAuthenticationProviderResolver")
            final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new OidcMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver, applicationContext);
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
        public HandlerInterceptor requiresAuthenticationDynamicRegistrationInterceptor(
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig) {
            val clients = String.join(",",
                Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN,
                Authenticators.CAS_OAUTH_CLIENT_ACCESS_TOKEN_AUTHN,
                Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM,
                Authenticators.CAS_OAUTH_CLIENT_USER_FORM);

            val interceptor = new SecurityInterceptor(oauthSecConfig, clients, JEEHttpActionAdapter.INSTANCE);
            interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
            interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
            return interceptor;
        }

        @Bean
        public HandlerInterceptor requiresAuthenticationClientConfigurationInterceptor(
            @Qualifier("oauthSecConfig")
            final Config oauthSecConfig) {
            val clients = String.join(",", OidcConstants.CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN);
            val interceptor = new SecurityInterceptor(oauthSecConfig, clients, JEEHttpActionAdapter.INSTANCE);
            interceptor.setMatchers(DefaultMatchers.SECURITYHEADERS);
            interceptor.setAuthorizers(DefaultAuthorizers.IS_FULLY_AUTHENTICATED);
            return interceptor;
        }

        @Bean
        public HandlerInterceptor oauthInterceptor(
            final ObjectProvider<List<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors,
            final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthRequestValidators,
            @Qualifier("oauthDistributedSessionStore")
            final SessionStore oauthDistributedSessionStore,
            @Qualifier("requiresAuthenticationAuthorizeInterceptor")
            final HandlerInterceptor requiresAuthenticationAuthorizeInterceptor,
            @Qualifier("requiresAuthenticationAccessTokenInterceptor")
            final HandlerInterceptor requiresAuthenticationAccessTokenInterceptor,
            @Qualifier("requiresAuthenticationClientConfigurationInterceptor")
            final HandlerInterceptor requiresAuthenticationClientConfigurationInterceptor,
            @Qualifier("requiresAuthenticationDynamicRegistrationInterceptor")
            final HandlerInterceptor requiresAuthenticationDynamicRegistrationInterceptor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            val mode = OidcConstants.DynamicClientRegistrationMode.valueOf(StringUtils.defaultIfBlank(
                oidc.getCore().getDynamicClientRegistrationMode(),
                OidcConstants.DynamicClientRegistrationMode.PROTECTED.name()));

            return new OidcHandlerInterceptorAdapter(
                requiresAuthenticationAccessTokenInterceptor,
                requiresAuthenticationAuthorizeInterceptor,
                requiresAuthenticationDynamicRegistrationInterceptor,
                requiresAuthenticationClientConfigurationInterceptor,
                mode,
                accessTokenGrantRequestExtractors,
                servicesManager,
                oauthDistributedSessionStore,
                oauthRequestValidators);
        }
    }

    @Configuration(value = "OidcEndpointsWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsWebConfiguration {
        private static String getOidcBaseEndpoint(final OidcIssuerService issuerService,
                                                  final CasConfigurationProperties casProperties) {
            val issuer = issuerService.determineIssuer(Optional.empty());
            val endpoint = StringUtils.remove(issuer, casProperties.getServer().getPrefix());
            return StringUtils.prependIfMissing(endpoint, "/");
        }

        @Bean
        public WebMvcConfigurer oidcWebMvcConfigurer(
            @Qualifier("oidcIssuerService")
            final OidcIssuerService oidcIssuerService,
            @Qualifier("oauthInterceptor")
            final ObjectProvider<HandlerInterceptor> oauthInterceptor,
            final CasConfigurationProperties casProperties) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(final InterceptorRegistry registry) {
                    val baseEndpoint = getOidcBaseEndpoint(oidcIssuerService, casProperties);
                    registry.addInterceptor(oauthInterceptor.getObject())
                        .order(100)
                        .addPathPatterns(baseEndpoint.concat("/*"));
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcProtocolEndpointConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ProtocolEndpointWebSecurityConfigurer<Void> oidcProtocolEndpointConfigurer(
            @Qualifier("oidcIssuerService")
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
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties) {
            val interceptor = new OidcLocaleChangeInterceptor(casProperties.getLocale(), argumentExtractor, servicesManager);
            interceptor.setParamName(OidcConstants.UI_LOCALES);
            return interceptor;
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcConfirmView")
        public View oidcConfirmView(final ConfigurableApplicationContext applicationContext,
                                    @Qualifier("casProtocolViewFactory")
                                    final CasProtocolViewFactory casProtocolViewFactory) {
            return casProtocolViewFactory.create(applicationContext, "protocol/oidc/confirm");
        }
    }

    @Configuration(value = "OidcEndpointsJwksRotationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsJwksRotationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRotationService")
        public OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcDefaultJsonWebKeystoreRotationService(oidc, applicationContext);
        }


        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRotationScheduler")
        @Bean
        @ConditionalOnProperty(prefix = "cas.authn.oidc.jwks.rotation.schedule",
            name = "enabled", havingValue = "true", matchIfMissing = true)
        public Runnable oidcJsonWebKeystoreRotationScheduler(
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return new OidcJsonWebKeystoreRotationScheduler(oidcJsonWebKeystoreRotationService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRevocationScheduler")
        @Bean
        @ConditionalOnProperty(prefix = "cas.authn.oidc.jwks.revocation.schedule",
            name = "enabled", havingValue = "true", matchIfMissing = true)
        public Runnable oidcJsonWebKeystoreRevocationScheduler(
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return new OidcJsonWebKeystoreRevocationScheduler(oidcJsonWebKeystoreRotationService);
        }

        @RequiredArgsConstructor
        @Slf4j
        public static class OidcJsonWebKeystoreRotationScheduler implements Runnable {
            private final OidcJsonWebKeystoreRotationService rotationService;

            @Scheduled(initialDelayString = "${cas.authn.oidc.jwks.rotation.schedule.start-delay:PT60S}",
                fixedDelayString = "${cas.authn.oidc.jwks.rotation.schedule.repeat-interval:P90D}")
            @Override
            @SneakyThrows
            public void run() {
                LOGGER.info("Starting to rotate keys in the OIDC keystore...");
                rotationService.rotate();
            }
        }

        @RequiredArgsConstructor
        @Slf4j
        public static class OidcJsonWebKeystoreRevocationScheduler implements Runnable {
            private final OidcJsonWebKeystoreRotationService rotationService;

            @Scheduled(initialDelayString = "${cas.authn.oidc.jwks.revocation.schedule.start-delay:PT60S}",
                fixedDelayString = "${cas.authn.oidc.jwks.revocation.schedule.repeat-interval:P14D}")
            @Override
            @SneakyThrows
            public void run() {
                LOGGER.info("Starting to revoke keys in the OIDC keystore...");
                rotationService.revoke();
            }
        }
    }

    @Configuration(value = "OidcEndpointsJwksGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsJwksGeneratorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCacheLoader")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CacheLoader<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCacheLoader(
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService) {
            return new OidcDefaultJsonWebKeystoreCacheLoader(oidcJsonWebKeystoreGeneratorService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeyStoreListener")
        @Bean
        public CasEventListener oidcJsonWebKeyStoreListener(
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCache) {
            return new OidcJsonWebKeyStoreListener(oidcDefaultJsonWebKeystoreCache);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCache")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LoadingCache<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCache(
            @Qualifier("oidcDefaultJsonWebKeystoreCacheLoader")
            final CacheLoader<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCacheLoader,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();

            val expiration = Beans.newDuration(oidc.getJwks().getJwksCacheExpiration());
            return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(expiration)
                .build(oidcDefaultJsonWebKeystoreCacheLoader);
        }

        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreGeneratorService")
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcDefaultJsonWebKeystoreGeneratorService(oidc, applicationContext);
        }
    }

    @Configuration(value = "OidcEndpointsJwksRestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(name = "cas.authn.oidc.jwks.rest.url")
    public static class OidcEndpointsJwksRestConfiguration {
        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcRestfulJsonWebKeystoreGeneratorService(oidc);
        }
    }

    @Configuration(value = "OidcEndpointsJwksGroovyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(name = "cas.authn.oidc.jwks.groovy.location")
    public static class OidcEndpointsJwksGroovyConfiguration {
        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcGroovyJsonWebKeystoreGeneratorService(oidc.getJwks().getGroovy().getLocation());
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
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext,
            @Qualifier("oidcWebFingerDiscoveryService")
            final OidcWebFingerDiscoveryService oidcWebFingerDiscoveryService) {
            return new OidcWellKnownEndpointController(oidcConfigurationContext, oidcWebFingerDiscoveryService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcProfileController")
        @Bean
        public OidcUserProfileEndpointController oidcProfileController(
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcUserProfileEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public OidcAuthorizeEndpointController oidcAuthorizeController(
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcAuthorizeEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcLogoutEndpointController")
        public OidcLogoutEndpointController oidcLogoutEndpointController(
            @Qualifier(OidcPostLogoutRedirectUrlMatcher.BEAN_NAME_POST_LOGOUT_REDIRECT_URL_MATCHER)
            final OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher,
            @Qualifier("urlValidator")
            final UrlValidator urlValidator,
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcLogoutEndpointController(oidcConfigurationContext,
                postLogoutRedirectUrlMatcher, urlValidator);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcRevocationEndpointController")
        public OidcRevocationEndpointController oidcRevocationEndpointController(
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcRevocationEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcAccessTokenController")
        public OidcAccessTokenEndpointController oidcAccessTokenController(
            @Qualifier("accessTokenGrantAuditableRequestExtractor")
            final AuditableExecution accessTokenGrantAuditableRequestExtractor,
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcAccessTokenEndpointController(oidcConfigurationContext,
                accessTokenGrantAuditableRequestExtractor);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcDynamicClientRegistrationEndpointController")
        public OidcDynamicClientRegistrationEndpointController oidcDynamicClientRegistrationEndpointController(
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcDynamicClientRegistrationEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcClientConfigurationEndpointController")
        @Bean
        public OidcClientConfigurationEndpointController oidcClientConfigurationEndpointController(
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcClientConfigurationEndpointController(oidcConfigurationContext);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJwksController")
        @Bean
        public OidcJwksEndpointController oidcJwksController(
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService,
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcJwksEndpointController(oidcConfigurationContext, oidcJsonWebKeystoreGeneratorService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "oidcIntrospectionEndpointController")
        public OidcIntrospectionEndpointController oidcIntrospectionEndpointController(
            @Qualifier("oidcConfigurationContext")
            final OidcConfigurationContext oidcConfigurationContext) {
            return new OidcIntrospectionEndpointController(oidcConfigurationContext);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public OidcJwksRotationEndpoint jwksRotationEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return new OidcJwksRotationEndpoint(casProperties, oidcJsonWebKeystoreRotationService);
        }
    }

    @Configuration(value = "OidcEndpointsWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsWebflowConfiguration {

        @ConditionalOnMissingBean(name = "oidcCasWebflowExecutionPlanConfigurer")
        @Bean
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
            @Qualifier("argumentExtractor")
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

        @ConditionalOnMissingBean(name = "oidcRegisteredServiceUIAction")
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
