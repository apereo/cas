package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.FileSystemSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.DefaultSamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
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
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlPostProfileHandlerEndpoint;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;

import java.net.URL;
import java.util.Optional;

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
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlResponseBuilder;

    @Lazy
    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @DependsOn("samlIdPMetadataGenerator")
    @SneakyThrows
    @Autowired
    public MetadataResolver casSamlIdPMetadataResolver(@Qualifier("samlIdPMetadataLocator") final SamlIdPMetadataLocator samlMetadataLocator) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val resolver = new InMemoryResourceMetadataResolver(samlMetadataLocator.resolveMetadata(Optional.empty()),
            openSamlConfigBean.getObject());
        resolver.setFailFastInitialization(idp.getMetadata().isFailFast());
        resolver.setRequireValidMetadata(idp.getMetadata().isRequireValidMetadata());
        resolver.setId(idp.getEntityId());
        return resolver;
    }

    @Bean
    @RefreshScope
    public SamlIdPMetadataController samlIdPMetadataController() {
        return new SamlIdPMetadataController(samlIdPMetadataGenerator(), samlIdPMetadataLocator());
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataGenerator")
    @Bean
    @SneakyThrows
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter())
            .entityId(idp.getEntityId())
            .resourceLoader(resourceLoader)
            .casServerPrefix(casProperties.getServer().getPrefix())
            .scope(idp.getScope())
            .metadataCipherExecutor(CipherExecutor.noOpOfStringToString())
            .build();
        return new FileSystemSamlIdPMetadataGenerator(context);
    }

    @ConditionalOnMissingBean(name = "samlSelfSignedCertificateWriter")
    @Bean
    @SneakyThrows
    public SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter() {
        val url = new URL(casProperties.getServer().getPrefix());
        val generator = new DefaultSamlIdPCertificateAndKeyWriter();
        generator.setHostname(url.getHost());
        generator.setUriSubjectAltNames(CollectionUtils.wrap(url.getHost().concat("/idp/metadata")));
        return generator;
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataLocator")
    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new FileSystemSamlIdPMetadataLocator(idp.getMetadata().getLocation());
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

    @ConditionalOnMissingBean(name = "defaultSamlRegisteredServiceCachingMetadataResolver")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() {
        return new SamlRegisteredServiceDefaultCachingMetadataResolver(
            casProperties.getAuthn().getSamlIdp().getMetadata().getCacheExpirationMinutes(),
            chainingMetadataResolverCacheLoader()
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
    public SSOSamlPostProfileHandlerEndpoint ssoSamlPostProfileHandlerEndpoint() {
        return new SSOSamlPostProfileHandlerEndpoint(casProperties,
            servicesManager.getObject(),
            authenticationSystemSupport.getObject(),
            webApplicationServiceFactory.getObject(),
            PrincipalFactoryUtils.newPrincipalFactory(),
            samlProfileSamlResponseBuilder.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver(),
            new NonInflatingSaml20ObjectBuilder(openSamlConfigBean.getObject()));
    }
}
