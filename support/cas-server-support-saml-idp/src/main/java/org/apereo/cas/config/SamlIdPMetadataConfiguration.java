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
@Configuration("samlIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdPMetadataConfiguration {
    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("samlIdPServiceFactory")
    private ObjectProvider<ServiceFactory> samlIdPServiceFactory;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlResponseBuilder;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private ObjectProvider<VelocityEngine> velocityEngineFactoryBean;

    @Lazy
    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @DependsOn("samlIdPMetadataGenerator")
    public MetadataResolver casSamlIdPMetadataResolver() throws Exception {
        val idp = casProperties.getAuthn().getSamlIdp();
        val resolver = new SamlIdPMetadataResolver(samlIdPMetadataLocator(),
            samlIdPMetadataGenerator(), openSamlConfigBean.getObject(), casProperties);
        resolver.setFailFastInitialization(idp.getMetadata().getCore().isFailFast());
        resolver.setRequireValidMetadata(idp.getMetadata().getCore().isRequireValidMetadata());
        resolver.setId(idp.getCore().getEntityId());
        return resolver;
    }

    @Lazy
    @Bean
    @RefreshScope
    public SamlIdPMetadataController samlIdPMetadataController() throws Exception {
        return new SamlIdPMetadataController(samlIdPMetadataGenerator(),
            samlIdPMetadataLocator(), servicesManager.getObject(),
            webApplicationServiceFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataGenerator")
    @Bean
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() throws Exception {
        return new FileSystemSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext());
    }

    @ConditionalOnMissingBean(name = "samlSelfSignedCertificateWriter")
    @Bean
    public SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter() throws Exception {
        val url = new URL(casProperties.getServer().getPrefix());
        val generator = new DefaultSamlIdPCertificateAndKeyWriter();
        generator.setHostname(url.getHost());
        generator.setUriSubjectAltNames(CollectionUtils.wrap(url.getHost().concat("/idp/metadata")));
        return generator;
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataLocator")
    @Bean
    public SamlIdPMetadataLocator samlIdPMetadataLocator() throws Exception {
        val idp = casProperties.getAuthn().getSamlIdp();
        val location = SpringExpressionLanguageValueResolver.getInstance()
            .resolve(idp.getMetadata().getFileSystem().getLocation());
        val metadataLocation = ResourceUtils.getRawResourceFrom(location);
        return new FileSystemSamlIdPMetadataLocator(metadataLocation, samlIdPMetadataCache());
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataCache")
    @Bean
    @RefreshScope
    public Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(100)
            .expireAfterAccess(Beans.newDuration(idp.getMetadata().getCore().getCacheExpiration()))
            .build();
    }

    @ConditionalOnMissingBean(name = "chainingMetadataResolverCacheLoader")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolverCacheLoader chainingMetadataResolverCacheLoader() {
        return new SamlRegisteredServiceMetadataResolverCacheLoader(
            openSamlConfigBean.getObject(),
            httpClient.getObject(),
            samlRegisteredServiceMetadataResolvers());
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataResolvers")
    @Bean
    public SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers() {
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();

        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val cfgBean = openSamlConfigBean.getObject();
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
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() {
        return new SamlRegisteredServiceDefaultCachingMetadataResolver(
            Beans.newDuration(casProperties.getAuthn().getSamlIdp().getMetadata().getCore().getCacheExpiration()),
            chainingMetadataResolverCacheLoader(),
            openSamlConfigBean.getObject()
        );
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataHealthIndicator")
    @Bean
    @ConditionalOnEnabledHealthIndicator("samlRegisteredServiceMetadataHealthIndicator")
    public HealthIndicator samlRegisteredServiceMetadataHealthIndicator() {
        return new SamlRegisteredServiceMetadataHealthIndicator(samlRegisteredServiceMetadataResolvers(),
            servicesManager.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public SamlRegisteredServiceCachedMetadataEndpoint samlRegisteredServiceCachedMetadataEndpoint() {
        return new SamlRegisteredServiceCachedMetadataEndpoint(casProperties, defaultSamlRegisteredServiceCachingMetadataResolver(),
            servicesManager.getObject(), registeredServiceAccessStrategyEnforcer.getObject(),
            openSamlConfigBean.getObject());
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public SSOSamlIdPPostProfileHandlerEndpoint ssoSamlPostProfileHandlerEndpoint() {
        return new SSOSamlIdPPostProfileHandlerEndpoint(casProperties,
            servicesManager.getObject(),
            authenticationSystemSupport.getObject(),
            samlIdPServiceFactory.getObject(),
            PrincipalFactoryUtils.newPrincipalFactory(),
            samlProfileSamlResponseBuilder.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver(),
            new NonInflatingSaml20ObjectBuilder(openSamlConfigBean.getObject()));
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPMetadataGeneratorCipherExecutor")
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor() {
        return CipherExecutor.noOpOfStringToString();
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPMetadataGeneratorConfigurationContext")
    public SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext() throws Exception {
        return SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter())
            .applicationContext(applicationContext)
            .metadataCipherExecutor(samlIdPMetadataGeneratorCipherExecutor())
            .casProperties(casProperties)
            .openSamlConfigBean(openSamlConfigBean.getObject())
            .velocityEngine(velocityEngineFactoryBean.getObject())
            .build();
    }
}
