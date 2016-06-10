package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlIdPApplicationContextWrapper;
import org.apereo.cas.support.saml.services.SamlIdPEntityIdValidationServiceSelectionStrategy;
import org.apereo.cas.support.saml.services.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.services.idp.metadata.cache.ChainingMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.DefaultSamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdpMetadataAndCertificatesGenerationService;
import org.apereo.cas.support.saml.web.idp.metadata.ShibbolethIdpMetadataAndCertificatesGenerationService;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.DefaultAuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAssertionBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAttributeStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAuthNStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlConditionsBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlNameIdBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlSubjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlIdPConfiguration")
public class SamlIdPConfiguration {
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public Resource templateSpMetadata() {
        return new ClassPathResource("template-sp-metadata.xml");
    }

    /**
     * Saml id p single logout service logout url builder saml id p single logout service logout url builder.
     *
     * @return the saml idp single logout service logout url builder
     */
    @Bean(name={"defaultSingleLogoutServiceLogoutUrlBuilder",
                "samlIdPSingleLogoutServiceLogoutUrlBuilder"})
    public SamlIdPSingleLogoutServiceLogoutUrlBuilder samlIdPSingleLogoutServiceLogoutUrlBuilder() {
        return new SamlIdPSingleLogoutServiceLogoutUrlBuilder();
    }
    
    @Bean
    public BaseApplicationContextWrapper samlIdPApplicationContextWrapper() {
        return new SamlIdPApplicationContextWrapper();
    }

    @Bean
    public ValidationServiceSelectionStrategy samlIdPEntityIdValidationServiceSelectionStrategy() {
        return new SamlIdPEntityIdValidationServiceSelectionStrategy();
    }

    @Bean
    @RefreshScope
    public ChainingMetadataResolverCacheLoader chainingMetadataResolverCacheLoader() {
        final ChainingMetadataResolverCacheLoader c = new ChainingMetadataResolverCacheLoader();
        
        c.setFailFastInitialization(casProperties.getAuthn().getSamlIdp().getMetadata().isFailFast());
        c.setMetadataCacheExpirationMinutes(casProperties.getAuthn().getSamlIdp().getMetadata().getCacheExpirationMinutes());
        c.setRequireValidMetadata(casProperties.getAuthn().getSamlIdp().getMetadata().isRequireValidMetadata());
        
        return c;
    }

    @Bean
    @RefreshScope
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() {
        final DefaultSamlRegisteredServiceCachingMetadataResolver r =
                new DefaultSamlRegisteredServiceCachingMetadataResolver();
        r.setChainingMetadataResolverCacheLoader(chainingMetadataResolverCacheLoader());
        r.setMetadataCacheExpirationMinutes(casProperties.getAuthn().getSamlIdp().getMetadata().getCacheExpirationMinutes());
        return r;
    }
    
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder() {
        return new SamlProfileSamlResponseBuilder();
    }
    
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder() {
        return new SamlProfileSamlSubjectBuilder();
    }
    
    @Bean
    @RefreshScope
    public SamlObjectEncrypter samlObjectEncrypter() {
        return new SamlObjectEncrypter();
    }
    
    @Bean
    @RefreshScope
    public SamlObjectSigner samlObjectSigner() {
        return new SamlObjectSigner();
    }
    
    @Bean
    public SamlIdpMetadataAndCertificatesGenerationService shibbolethIdpMetadataAndCertificatesGenerationService() {
        final ShibbolethIdpMetadataAndCertificatesGenerationService s =
                new ShibbolethIdpMetadataAndCertificatesGenerationService();
        
        s.setEntityId(casProperties.getAuthn().getSamlIdp().getEntityId());
        s.setHostName(casProperties.getAuthn().getSamlIdp().getHostName());
        s.setMetadataLocation(casProperties.getAuthn().getSamlIdp().getMetadata().getLocation());
        s.setScope(casProperties.getAuthn().getSamlIdp().getScope());
        
        return s;
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder() {
        return new SamlProfileSamlNameIdBuilder();
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder() {
        return new SamlProfileSamlConditionsBuilder();
    }

    @Bean
    @RefreshScope
    public AuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder() {
        return new DefaultAuthnContextClassRefBuilder();
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder() {
        return new SamlProfileSamlAssertionBuilder();
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder() {
        return new SamlProfileSamlAuthNStatementBuilder();
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder() {
        return new SamlProfileSamlAttributeStatementBuilder();
    }
}
