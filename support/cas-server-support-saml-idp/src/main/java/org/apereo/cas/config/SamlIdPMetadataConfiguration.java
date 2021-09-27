package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataResolver;
import org.apereo.cas.support.saml.idp.metadata.writer.DefaultSamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataHealthIndicator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceDefaultCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.GroovyResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.JsonResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.MetadataQueryProtocolMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.support.saml.util.NonInflatingSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdPMetadataController;
import org.apereo.cas.support.saml.web.idp.metadata.SamlRegisteredServiceCachedMetadataEndpoint;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostProfileHandlerEndpoint;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import java.net.URL;

/**
 * This is {@link SamlIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "samlIdPMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPMetadataConfiguration {

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlResponseBuilder;

    @Lazy
    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @DependsOn("samlIdPMetadataGenerator")
    @Autowired
    public MetadataResolver casSamlIdPMetadataResolver(
        final CasConfigurationProperties casProperties,
        @Qualifier("samlIdPMetadataLocator")
        final SamlIdPMetadataLocator samlIdPMetadataLocator,
        @Qualifier("samlIdPMetadataGenerator")
        final SamlIdPMetadataGenerator samlIdPMetadataGenerator,
        @Qualifier("openSamlConfigBean")
        final OpenSamlConfigBean openSamlConfigBean) throws Exception {
        val idp = casProperties.getAuthn().getSamlIdp();
        val resolver = new SamlIdPMetadataResolver(samlIdPMetadataLocator, samlIdPMetadataGenerator, openSamlConfigBean, casProperties);
        resolver.setFailFastInitialization(idp.getMetadata().getCore().isFailFast());
        resolver.setRequireValidMetadata(idp.getMetadata().getCore().isRequireValidMetadata());
        resolver.setId(idp.getCore().getEntityId());
        return resolver;
    }

    @Lazy
    @Bean
    @RefreshScope
    public SamlIdPMetadataController samlIdPMetadataController(
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier("samlIdPMetadataGenerator")
        final SamlIdPMetadataGenerator samlIdPMetadataGenerator,
        @Qualifier("samlIdPMetadataLocator")
        final SamlIdPMetadataLocator samlIdPMetadataLocator,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) throws Exception {
        return new SamlIdPMetadataController(samlIdPMetadataGenerator, samlIdPMetadataLocator, servicesManager, webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataGenerator")
    @Bean
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) throws Exception {
        return new FileSystemSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext);
    }

    @ConditionalOnMissingBean(name = "samlSelfSignedCertificateWriter")
    @Bean
    @Autowired
    public SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter(final CasConfigurationProperties casProperties) throws Exception {
        val url = new URL(casProperties.getServer().getPrefix());
        val generator = new DefaultSamlIdPCertificateAndKeyWriter();
        generator.setHostname(url.getHost());
        generator.setUriSubjectAltNames(CollectionUtils.wrap(url.getHost().concat("/idp/metadata")));
        return generator;
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataLocator")
    @Bean
    @Autowired
    public SamlIdPMetadataLocator samlIdPMetadataLocator(final CasConfigurationProperties casProperties,
                                                         @Qualifier("samlIdPMetadataCache")
                                                         final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache) throws Exception {
        val idp = casProperties.getAuthn().getSamlIdp();
        val location = SpringExpressionLanguageValueResolver.getInstance().resolve(idp.getMetadata().getFileSystem().getLocation());
        val metadataLocation = ResourceUtils.getRawResourceFrom(location);
        return new FileSystemSamlIdPMetadataLocator(metadataLocation, samlIdPMetadataCache);
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataCache")
    @Bean
    @RefreshScope
    @Autowired
    public Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache(final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return Caffeine.newBuilder().initialCapacity(10)
            .maximumSize(100).expireAfterAccess(Beans.newDuration(idp.getMetadata().getCore().getCacheExpiration())).build();
    }

    @ConditionalOnMissingBean(name = "chainingMetadataResolverCacheLoader")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolverCacheLoader chainingMetadataResolverCacheLoader(
        @Qualifier("samlRegisteredServiceMetadataResolvers")
        final SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers,
        @Qualifier("httpClient")
        final HttpClient httpClient,
        @Qualifier("openSamlConfigBean")
        final OpenSamlConfigBean openSamlConfigBean) {
        return new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, samlRegisteredServiceMetadataResolvers);
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataResolvers")
    @Bean
    @Autowired
    public SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("openSamlConfigBean")
        final OpenSamlConfigBean openSamlConfigBean) {
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean;
        plan.registerMetadataResolver(new MetadataQueryProtocolMetadataResolver(samlIdp, cfgBean));
        plan.registerMetadataResolver(new JsonResourceMetadataResolver(samlIdp, cfgBean));
        plan.registerMetadataResolver(new FileSystemResourceMetadataResolver(samlIdp, cfgBean));
        plan.registerMetadataResolver(new UrlResourceMetadataResolver(samlIdp, cfgBean));
        plan.registerMetadataResolver(new ClasspathResourceMetadataResolver(samlIdp, cfgBean));
        plan.registerMetadataResolver(new GroovyResourceMetadataResolver(samlIdp, cfgBean));
        val configurers = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataResolutionPlanConfigurer.class, false, true);
        configurers.values().forEach(c -> {
            LOGGER.trace("Configuring saml metadata resolution plan [{}]", c.getName());
            c.configureMetadataResolutionPlan(plan);
        });
        return plan;
    }

    @ConditionalOnMissingBean(name = SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME)
    @Bean
    @RefreshScope
    @Autowired
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver(
        final CasConfigurationProperties casProperties,
        @Qualifier("chainingMetadataResolverCacheLoader")
        final SamlRegisteredServiceMetadataResolverCacheLoader chainingMetadataResolverCacheLoader,
        @Qualifier("openSamlConfigBean")
        final OpenSamlConfigBean openSamlConfigBean) {
        return new SamlRegisteredServiceDefaultCachingMetadataResolver(
            Beans.newDuration(casProperties.getAuthn().getSamlIdp().getMetadata().getCore().getCacheExpiration()),
            chainingMetadataResolverCacheLoader, openSamlConfigBean);
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataHealthIndicator")
    @Bean
    @ConditionalOnEnabledHealthIndicator("samlRegisteredServiceMetadataHealthIndicator")
    public HealthIndicator samlRegisteredServiceMetadataHealthIndicator(
        @Qualifier("samlRegisteredServiceMetadataResolvers")
        final SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) {
        return new SamlRegisteredServiceMetadataHealthIndicator(samlRegisteredServiceMetadataResolvers, servicesManager);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public SamlRegisteredServiceCachedMetadataEndpoint samlRegisteredServiceCachedMetadataEndpoint(
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
        final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("openSamlConfigBean")
        final OpenSamlConfigBean openSamlConfigBean,
        @Qualifier("registeredServiceAccessStrategyEnforcer")
        final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        return new SamlRegisteredServiceCachedMetadataEndpoint(casProperties,
            defaultSamlRegisteredServiceCachingMetadataResolver, servicesManager, registeredServiceAccessStrategyEnforcer,
            openSamlConfigBean);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public SSOSamlIdPPostProfileHandlerEndpoint ssoSamlPostProfileHandlerEndpoint(
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
        final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager,
        @Qualifier("openSamlConfigBean")
        final OpenSamlConfigBean openSamlConfigBean,
        @Qualifier("defaultAuthenticationSystemSupport")
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("samlIdPServiceFactory")
        final ServiceFactory samlIdPServiceFactory) {
        return new SSOSamlIdPPostProfileHandlerEndpoint(casProperties, servicesManager,
            authenticationSystemSupport, samlIdPServiceFactory, PrincipalFactoryUtils.newPrincipalFactory(),
            samlProfileSamlResponseBuilder.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver, new NonInflatingSaml20ObjectBuilder(openSamlConfigBean));
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPMetadataGeneratorCipherExecutor")
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor() {
        return CipherExecutor.noOpOfStringToString();
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPMetadataGeneratorConfigurationContext")
    @Autowired
    public SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("samlIdPMetadataLocator")
        final SamlIdPMetadataLocator samlIdPMetadataLocator,
        @Qualifier("samlSelfSignedCertificateWriter")
        final SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("openSamlConfigBean")
        final OpenSamlConfigBean openSamlConfigBean,
        @Qualifier("velocityEngineFactoryBean")
        final VelocityEngine velocityEngineFactoryBean) throws Exception {
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
