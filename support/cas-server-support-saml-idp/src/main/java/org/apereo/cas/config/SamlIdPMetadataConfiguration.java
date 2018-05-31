package org.apereo.cas.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.generator.FileSystemSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.DefaultSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.DefaultSamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceDefaultCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.DynamicMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.GroovyResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurator;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdPMetadataController;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Lazy
    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @DependsOn("samlIdPMetadataGenerator")
    @SneakyThrows
    @Autowired
    public MetadataResolver casSamlIdPMetadataResolver(@Qualifier("samlMetadataLocator") final SamlIdPMetadataLocator samlMetadataLocator) {
        final var idp = casProperties.getAuthn().getSamlIdp();
        final var resolver = new InMemoryResourceMetadataResolver(samlMetadataLocator.getMetadata(), openSamlConfigBean);
        resolver.setParserPool(this.openSamlConfigBean.getParserPool());
        resolver.setFailFastInitialization(idp.getMetadata().isFailFast());
        resolver.setRequireValidMetadata(idp.getMetadata().isRequireValidMetadata());
        resolver.setId(idp.getEntityId());
        return resolver;
    }

    @Bean
    @RefreshScope
    public SamlIdPMetadataController samlIdPMetadataController() {
        return new SamlIdPMetadataController(samlIdPMetadataGenerator(), samlMetadataLocator());
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataGenerator")
    @Bean
    @SneakyThrows
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        final var idp = casProperties.getAuthn().getSamlIdp();
        return new FileSystemSamlIdPMetadataGenerator(idp.getEntityId(), this.resourceLoader,
            casProperties.getServer().getPrefix(), idp.getScope(),
            samlMetadataLocator(), samlSelfSignedCertificateWriter());
    }

    @ConditionalOnMissingBean(name = "samlSelfSignedCertificateWriter")
    @Bean
    @SneakyThrows
    public SamlIdPCertificateAndKeyWriter samlSelfSignedCertificateWriter() {
        final var url = new URL(casProperties.getServer().getPrefix());
        final var generator = new DefaultSamlIdPCertificateAndKeyWriter();
        generator.setHostname(url.getHost());
        generator.setUriSubjectAltNames(CollectionUtils.wrap(url.getHost().concat("/idp/metadata")));
        return generator;
    }

    @ConditionalOnMissingBean(name = "samlMetadataLocator")
    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlMetadataLocator() {
        final var idp = casProperties.getAuthn().getSamlIdp();
        return new DefaultSamlIdPMetadataLocator(idp.getMetadata().getLocation());
    }

    @ConditionalOnMissingBean(name = "chainingMetadataResolverCacheLoader")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolverCacheLoader chainingMetadataResolverCacheLoader() {
        return new SamlRegisteredServiceMetadataResolverCacheLoader(
            openSamlConfigBean, httpClient,
            samlRegisteredServiceMetadataResolvers());
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataResolvers")
    @Bean
    public SamlRegisteredServiceMetadataResolutionPlan samlRegisteredServiceMetadataResolvers() {
        final var plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();

        final var samlIdp = casProperties.getAuthn().getSamlIdp();
        plan.registerMetadataResolver(new DynamicMetadataResolver(samlIdp, openSamlConfigBean));
        plan.registerMetadataResolver(new FileSystemResourceMetadataResolver(samlIdp, openSamlConfigBean));
        plan.registerMetadataResolver(new UrlResourceMetadataResolver(samlIdp, openSamlConfigBean));
        plan.registerMetadataResolver(new ClasspathResourceMetadataResolver(samlIdp, openSamlConfigBean));
        plan.registerMetadataResolver(new GroovyResourceMetadataResolver(samlIdp, openSamlConfigBean));

        final var configurers =
            this.applicationContext.getBeansOfType(SamlRegisteredServiceMetadataResolutionPlanConfigurator.class, false, true);

        configurers.values().forEach(c -> {
            final var name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring saml metadata resolution plan [{}]", name);
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

}
