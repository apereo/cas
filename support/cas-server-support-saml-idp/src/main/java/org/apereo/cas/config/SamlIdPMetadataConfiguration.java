package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.SamlIdPMetadataResolver;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.DefaultSamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataHealthIndicator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.CachedMetadataResolverResult;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCacheKey;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceDefaultCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.GroovyResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.JsonResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.MetadataQueryProtocolMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.support.saml.util.NonInflatingSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdPMetadataController;
import org.apereo.cas.support.saml.web.idp.metadata.SamlRegisteredServiceCachedMetadataEndpoint;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostProfileHandlerEndpoint;
import org.apereo.cas.support.saml.web.idp.web.SamlIdPErrorController;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.CasApplicationReadyListener;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.jspecify.annotations.NonNull;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link SamlIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider)
@Configuration(value = "SamlIdPMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPMetadataConfiguration {

    @Configuration(value = "SamlIdPMetadataEndpointConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPMetadataEndpointConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPErrorController samlIdPErrorController() {
            return new SamlIdPErrorController();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPMetadataController samlIdPMetadataController(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
            final SamlIdPMetadataGenerator samlIdPMetadataGenerator,
            @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
            final SamlIdPMetadataLocator samlIdPMetadataLocator,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new SamlIdPMetadataController(samlIdPMetadataGenerator,
                samlIdPMetadataLocator, servicesManager, webApplicationServiceFactory);
        }

        @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataHealthIndicator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnEnabledHealthIndicator("samlRegisteredServiceMetadataHealthIndicator")
        public HealthIndicator samlRegisteredServiceMetadataHealthIndicator(
            @Qualifier("samlRegisteredServiceMetadataResolvers")
            final SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new SamlRegisteredServiceMetadataHealthIndicator(samlRegisteredServiceMetadataResolvers, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint
        public SamlRegisteredServiceCachedMetadataEndpoint samlRegisteredServiceCachedMetadataEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
            final ObjectProvider<@NonNull SamlRegisteredServiceCachingMetadataResolver> defaultSamlRegisteredServiceCachingMetadataResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<@NonNull ServicesManager> servicesManager,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final ObjectProvider<@NonNull OpenSamlConfigBean> openSamlConfigBean,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final ObjectProvider<@NonNull AuditableExecution> registeredServiceAccessStrategyEnforcer) {
            return new SamlRegisteredServiceCachedMetadataEndpoint(casProperties,
                defaultSamlRegisteredServiceCachingMetadataResolver, servicesManager,
                registeredServiceAccessStrategyEnforcer, openSamlConfigBean);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint
        public SSOSamlIdPPostProfileHandlerEndpoint ssoSamlPostProfileHandlerEndpoint(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver,
            final CasConfigurationProperties casProperties,
            @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory) {
            return new SSOSamlIdPPostProfileHandlerEndpoint(casProperties,
                applicationContext, servicesManager,
                authenticationSystemSupport, samlIdPServiceFactory,
                PrincipalFactoryUtils.newPrincipalFactory(),
                samlProfileSamlResponseBuilder,
                defaultSamlRegisteredServiceCachingMetadataResolver,
                new NonInflatingSaml20ObjectBuilder(openSamlConfigBean),
                defaultPrincipalResolver, casSamlIdPMetadataResolver);
        }

    }

    @Configuration(value = "SamlIdPDefaultMetadataResolversConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPDefaultMetadataResolversConfiguration {

        @ConditionalOnMissingBean(name = "metadataQueryProtocolMetadataResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public SamlRegisteredServiceMetadataResolver metadataQueryProtocolMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient) {
            return new MetadataQueryProtocolMetadataResolver(httpClient, casProperties.getAuthn().getSamlIdp(), openSamlConfigBean);
        }

        @ConditionalOnMissingBean(name = "jsonResourceMetadataResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Order(Ordered.HIGHEST_PRECEDENCE + 1)
        public SamlRegisteredServiceMetadataResolver jsonResourceMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new JsonResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean);
        }

        @ConditionalOnMissingBean(name = "fileSystemResourceMetadataResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Order(Ordered.HIGHEST_PRECEDENCE + 2)
        public SamlRegisteredServiceMetadataResolver fileSystemResourceMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new FileSystemResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean);
        }

        @ConditionalOnMissingBean(name = "urlResourceMetadataResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Order(Ordered.HIGHEST_PRECEDENCE + 3)
        public SamlRegisteredServiceMetadataResolver urlResourceMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient) {
            return new UrlResourceMetadataResolver(httpClient, casProperties.getAuthn().getSamlIdp(), openSamlConfigBean);
        }

        @ConditionalOnMissingBean(name = "classpathResourceMetadataResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Order(Ordered.HIGHEST_PRECEDENCE + 4)
        public SamlRegisteredServiceMetadataResolver classpathResourceMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new ClasspathResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean);
        }

        @ConditionalOnMissingBean(name = "groovyResourceMetadataResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Order(Ordered.HIGHEST_PRECEDENCE + 5)
        @ConditionalOnMissingGraalVMNativeImage
        public SamlRegisteredServiceMetadataResolver groovyResourceMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new GroovyResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean);
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultSamlRegisteredServiceMetadataResolutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlRegisteredServiceMetadataResolutionPlanConfigurer defaultSamlRegisteredServiceMetadataResolutionPlanConfigurer(
            final List<SamlRegisteredServiceMetadataResolver> configurersList) {
            return plan -> configurersList.forEach(plan::registerMetadataResolver);
        }
    }

    @Configuration(value = "SamlIdPMetadataResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPMetadataResolutionConfiguration {
        @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataResolvers")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers(
            final ObjectProvider<@NonNull List<SamlRegisteredServiceMetadataResolutionPlanConfigurer>> configurersList) {
            val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
            val configurers = Optional.ofNullable(configurersList.getIfAvailable()).orElseGet(ArrayList::new);
            configurers.forEach(cfg -> {
                LOGGER.trace("Configuring saml metadata resolution plan [{}]", cfg.getName());
                cfg.configureMetadataResolutionPlan(plan);
            });
            return plan;
        }


        @Lazy
        @Bean(initMethod = "initialize", destroyMethod = "destroy")
        @DependsOn(SamlIdPMetadataGenerator.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MetadataResolver casSamlIdPMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
            final SamlIdPMetadataLocator samlIdPMetadataLocator,
            @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
            final SamlIdPMetadataGenerator samlIdPMetadataGenerator,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            val idp = casProperties.getAuthn().getSamlIdp();
            val resolver = new SamlIdPMetadataResolver(samlIdPMetadataLocator, samlIdPMetadataGenerator, openSamlConfigBean, casProperties);
            resolver.setFailFastInitialization(idp.getMetadata().getCore().isFailFast());
            resolver.setRequireValidMetadata(idp.getMetadata().getCore().isRequireValidMetadata());
            resolver.setId(idp.getCore().getEntityId());
            return resolver;
        }
    }

    @Configuration(value = "SamlIdPMetadataGenerationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class SamlIdPMetadataGenerationConfiguration {
        @ConditionalOnMissingBean(name = SamlIdPMetadataGenerator.BEAN_NAME)
        @Bean(initMethod = "initialize")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
            @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
            final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
            return new FileSystemSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext);
        }

        @ConditionalOnMissingBean(name = "samlSelfSignedCertificateWriter")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter(
            final CasConfigurationProperties casProperties) throws Exception {
            val properties = casProperties.getAuthn().getSamlIdp().getMetadata().getCore();
            val url = new URI(casProperties.getServer().getPrefix());
            val generator = new DefaultSamlIdPCertificateAndKeyWriter(url.getHost());
            generator.setUriSubjectAltNames(CollectionUtils.wrap(url.getHost().concat("/idp/metadata")));
            properties.setCertificateAlgorithm(properties.getCertificateAlgorithm());
            properties.setKeySize(properties.getKeySize());
            return generator;
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPMetadataGeneratorCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CipherExecutor samlIdPMetadataGeneratorCipherExecutor() {
            return CipherExecutor.noOpOfStringToString();
        }

    }

    @Configuration(value = "SamlIdPMetadataLocatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPMetadataLocatorConfiguration {
        @ConditionalOnMissingBean(name = SamlIdPMetadataLocator.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPMetadataLocator samlIdPMetadataLocator(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
            final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
            final CasConfigurationProperties casProperties,
            @Qualifier("samlIdPMetadataCache")
            final Cache<@NonNull String, SamlIdPMetadataDocument> samlIdPMetadataCache) throws Exception {
            val idp = casProperties.getAuthn().getSamlIdp();
            val location = SpringExpressionLanguageValueResolver.getInstance()
                .resolve(idp.getMetadata().getFileSystem().getLocation());
            val metadataLocation = ResourceUtils.getRawResourceFrom(location);
            return new FileSystemSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
                metadataLocation, samlIdPMetadataCache, applicationContext);
        }
    }

    @Configuration(value = "SamlIdPMetadataCacheConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPMetadataCacheConfiguration {

        @ConditionalOnMissingBean(name = "samlIdPMetadataCache")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Cache<@NonNull String, SamlIdPMetadataDocument> samlIdPMetadataCache(
            final CasConfigurationProperties casProperties) {
            val idp = casProperties.getAuthn().getSamlIdp();
            return Caffeine.newBuilder().initialCapacity(10)
                .maximumSize(100).expireAfterAccess(Beans.newDuration(idp.getMetadata().getCore().getCacheExpiration())).build();
        }

        @ConditionalOnMissingBean(name = "chainingMetadataResolverCacheLoader")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CacheLoader<@NonNull SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> chainingMetadataResolverCacheLoader(
            @Qualifier("samlRegisteredServiceMetadataResolvers")
            final SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean,
                httpClient, samlRegisteredServiceMetadataResolvers);
        }
    }

    @Configuration(value = "SamlIdPMetadataResolverConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPMetadataResolverConfiguration {

        @ConditionalOnMissingBean(name = SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver(
            final CasConfigurationProperties casProperties,
            @Qualifier("chainingMetadataResolverCacheLoader")
            final CacheLoader<@NonNull SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> chainingMetadataResolverCacheLoader,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlRegisteredServiceDefaultCachingMetadataResolver(
                casProperties, chainingMetadataResolverCacheLoader, openSamlConfigBean);
        }
    }

    @Configuration(value = "SamlIdPMetadataContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPMetadataContextConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPMetadataGeneratorConfigurationContext")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
            final SamlIdPMetadataLocator samlIdPMetadataLocator,
            @Qualifier("samlSelfSignedCertificateWriter")
            final SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter,
            @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
            final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("velocityEngineFactoryBean")
            final VelocityEngine velocityEngineFactoryBean) {
            return SamlIdPMetadataGeneratorConfigurationContext.builder()
                .samlIdPMetadataLocator(samlIdPMetadataLocator)
                .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter)
                .applicationContext(applicationContext)
                .metadataCipherExecutor(samlIdPMetadataGeneratorCipherExecutor)
                .casProperties(casProperties)
                .openSamlConfigBean(openSamlConfigBean)
                .velocityEngine(velocityEngineFactoryBean)
                .build();
        }
    }

    @Configuration(value = "SamlIdPMetadataInitializationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPMetadataInitializationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        @ConditionalOnMissingBean(name = "samlIdPCasEventListener")
        public CasApplicationReadyListener samlIdPCasEventListener(
            @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
            final SamlIdPMetadataGenerator samlIdPMetadataGenerator) {
            return event -> {
                val document = FunctionUtils.doAndHandle(() -> {
                    LOGGER.debug("Attempting to generate/fetch SAML IdP metadata...");
                    return samlIdPMetadataGenerator.generate(Optional.empty());
                }, e -> {
                    LoggingUtils.error(LOGGER, e);
                    return null;
                }).get();
                LOGGER.trace("Generated SAML IdP metadata document is [{}]", document);
            };
        }
    }
}
