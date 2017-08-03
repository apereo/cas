package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.DefaultAuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAssertionBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAttributeStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAuthNStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlConditionsBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlNameIdBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlSubjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlAttributeEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSaml2ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlSoap11FaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.web.UrlValidator;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.ecp.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactory;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlIdPConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("shibbolethCompatiblePersistentIdGenerator")
    private PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private VelocityEngineFactory velocityEngineFactory;
    
    @Autowired
    private UrlValidator urlValidator;

    @Bean
    public SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder() {
        return new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager, defaultSamlRegisteredServiceCachingMetadataResolver, urlValidator);
    }
    
    @ConditionalOnMissingBean(name = "samlProfileSamlResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder() {
        return new SamlProfileSaml2ResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSubjectBuilder")
    @Bean
    @RefreshScope
    public SamlProfileSamlSubjectBuilder samlProfileSamlSubjectBuilder() {
        return new SamlProfileSamlSubjectBuilder(openSamlConfigBean, samlProfileSamlNameIdBuilder(),
                casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance());
    }
    
    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11FaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11FaultResponseBuilder() {
        return new SamlProfileSamlSoap11FaultResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlProfileSamlResponseBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11ResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11ResponseBuilder() {
        return new SamlProfileSamlSoap11ResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlProfileSamlResponseBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlNameIdBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder() {
        return new SamlProfileSamlNameIdBuilder(openSamlConfigBean, shibbolethCompatiblePersistentIdGenerator);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlConditionsBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder() {
        return new SamlProfileSamlConditionsBuilder(openSamlConfigBean);
    }

    @ConditionalOnMissingBean(name = "defaultAuthnContextClassRefBuilder")
    @Bean
    @RefreshScope
    public AuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder() {
        return new DefaultAuthnContextClassRefBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAssertionBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder() {
        return new SamlProfileSamlAssertionBuilder(openSamlConfigBean,
                samlProfileSamlAuthNStatementBuilder(),
                samlProfileSamlAttributeStatementBuilder(),
                samlProfileSamlSubjectBuilder(),
                samlProfileSamlConditionsBuilder(),
                samlObjectSigner());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAuthNStatementBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder() {
        return new SamlProfileSamlAuthNStatementBuilder(openSamlConfigBean, defaultAuthnContextClassRefBuilder());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeStatementBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder() {
        return new SamlProfileSamlAttributeStatementBuilder(openSamlConfigBean, new SamlAttributeEncoder());
    }

    @ConditionalOnMissingBean(name = "samlObjectEncrypter")
    @Bean
    @RefreshScope
    public SamlObjectEncrypter samlObjectEncrypter() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlObjectEncrypter(algs.getOverrideDataEncryptionAlgorithms(),
                algs.getOverrideKeyEncryptionAlgorithms(),
                algs.getOverrideBlackListedEncryptionAlgorithms(),
                algs.getOverrideWhiteListedAlgorithms());
    }

    @ConditionalOnMissingBean(name = "samlObjectSigner")
    @Bean
    @RefreshScope
    public BaseSamlObjectSigner samlObjectSigner() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new BaseSamlObjectSigner(
                algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(),
                algs.getOverrideBlackListedSignatureSigningAlgorithms(),
                algs.getOverrideWhiteListedSignatureSigningAlgorithms());
    }

}
