package org.apereo.cas.config;

import net.shibboleth.ext.spring.resource.ResourceHelper;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceDefaultCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.DynamicMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolver;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdpMetadataAndCertificatesGenerationService;
import org.apereo.cas.support.saml.web.idp.metadata.SamlMetadataController;
import org.apereo.cas.support.saml.web.idp.metadata.TemplatedMetadataAndCertificatesGenerationService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.ResourceBackedMetadataResolver;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;

import java.util.Collection;

/**
 * This is {@link SamlIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPMetadataConfiguration {
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Lazy
    @Bean
    @DependsOn("shibbolethIdpMetadataAndCertificatesGenerationService")
    public MetadataResolver casSamlIdPMetadataResolver() {
        try {
            final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
            final ResourceBackedMetadataResolver resolver = new ResourceBackedMetadataResolver(
                ResourceHelper.of(new FileSystemResource(idp.getMetadata().getMetadataFile())));
            resolver.setParserPool(this.openSamlConfigBean.getParserPool());
            resolver.setFailFastInitialization(idp.getMetadata().isFailFast());
            resolver.setRequireValidMetadata(idp.getMetadata().isRequireValidMetadata());
            resolver.setId(idp.getEntityId());
            resolver.initialize();
            return resolver;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    @RefreshScope
    public SamlMetadataController samlMetadataController() {
        return new SamlMetadataController(shibbolethIdpMetadataAndCertificatesGenerationService());
    }

    @ConditionalOnMissingBean(name = "shibbolethIdpMetadataAndCertificatesGenerationService")
    @Bean
    public SamlIdpMetadataAndCertificatesGenerationService shibbolethIdpMetadataAndCertificatesGenerationService() {
        return new TemplatedMetadataAndCertificatesGenerationService();
    }

    @ConditionalOnMissingBean(name = "chainingMetadataResolverCacheLoader")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolverCacheLoader chainingMetadataResolverCacheLoader()
        throws Exception {
        return new SamlRegisteredServiceMetadataResolverCacheLoader(
            openSamlConfigBean, httpClient,
            casProperties.getAuthn().getSamlIdp(),
            samlRegisteredServiceMetadataResolvers());
    }

    @ConditionalOnMissingBean(name = "samlRegisteredServiceMetadataResolvers")
    @Bean
    @RefreshScope
    public Collection<SamlRegisteredServiceMetadataResolver> samlRegisteredServiceMetadataResolvers()
        throws Exception {
        return CollectionUtils.wrapSet(
            new DynamicMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean, httpClient),
            new FileSystemResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean),
            new UrlResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean),
            new ClasspathResourceMetadataResolver(casProperties.getAuthn().getSamlIdp(), openSamlConfigBean)
        );
    }

    @ConditionalOnMissingBean(name = "defaultSamlRegisteredServiceCachingMetadataResolver")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver()
        throws Exception {
        return new SamlRegisteredServiceDefaultCachingMetadataResolver(
            casProperties.getAuthn().getSamlIdp().getMetadata().getCacheExpirationMinutes(),
            chainingMetadataResolverCacheLoader()
        );
    }

}
