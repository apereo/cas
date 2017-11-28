package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.artifact.CasSamlArtifactMap;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.assertion.SamlProfileSamlAssertionBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlProfileSamlAttributeStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.authn.AuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.authn.DefaultAuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.authn.SamlProfileSamlAuthNStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.conditions.SamlProfileSamlConditionsBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlAttributeEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.nameid.SamlProfileSamlNameIdBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSaml2ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact.SamlProfileArtifactFaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.artifact.SamlProfileArtifactResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.query.SamlProfileAttributeQueryFaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.query.SamlProfileAttributeQueryResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11FaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.subject.SamlProfileSamlSubjectBuilder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.artifact.DefaultSamlArtifactTicketFactory;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketExpirationPolicy;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.DefaultSamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketExpirationPolicy;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.opensaml.saml.common.binding.artifact.SAMLArtifactMap;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.ecp.Response;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactory;

import java.util.concurrent.TimeUnit;

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
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    private MetadataResolver casSamlIdPMetadataResolver;
    
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
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;
    
    @Autowired
    @Qualifier("urlValidator")
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
                samlObjectEncrypter(),
                ticketRegistry,
                samlArtifactTicketFactory(), 
                ticketGrantingTicketCookieGenerator, 
                samlArtifactMap(), 
                samlAttributeQueryTicketFactory());
    }

    @ConditionalOnMissingBean(name = "samlArtifactTicketFactory")
    @Bean
    @RefreshScope
    public SamlArtifactTicketFactory samlArtifactTicketFactory() {
        return new DefaultSamlArtifactTicketFactory(samlArtifactTicketExpirationPolicy(), 
                openSamlConfigBean,
                webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "samlArtifactTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy samlArtifactTicketExpirationPolicy() {
        return new SamlArtifactTicketExpirationPolicy(casProperties.getTicket().getSt().getTimeToKillInSeconds());
    }

    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    @RefreshScope
    public SAMLArtifactMap samlArtifactMap() {
        try {
            final CasSamlArtifactMap map = new CasSamlArtifactMap(ticketRegistry, samlArtifactTicketFactory(),
                    ticketGrantingTicketCookieGenerator);
            map.setArtifactLifetime(TimeUnit.SECONDS.toMillis(samlArtifactTicketExpirationPolicy().getTimeToLive()));
            return map;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }
    
    @ConditionalOnMissingBean(name = "samlProfileSamlSubjectBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Subject> samlProfileSamlSubjectBuilder() {
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

    
    @ConditionalOnMissingBean(name = "samlProfileSamlArtifactFaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactFaultResponseBuilder() {
        return new SamlProfileArtifactFaultResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlProfileSamlResponseBuilder(),
                samlObjectEncrypter());
    }
    
    @ConditionalOnMissingBean(name = "samlProfileSamlArtifactResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlArtifactResponseBuilder() {
        return new SamlProfileArtifactResponseBuilder(
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
                algs.getOverrideWhiteListedSignatureSigningAlgorithms(),
                casSamlIdPMetadataResolver);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryFaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryFaultResponseBuilder() {
        return new SamlProfileAttributeQueryFaultResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlProfileSamlResponseBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeQueryResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlAttributeQueryResponseBuilder() {
        return new SamlProfileAttributeQueryResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlProfileSamlResponseBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlAttributeQueryTicketFactory")
    @Bean
    @RefreshScope
    public SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory() {
        return new DefaultSamlAttributeQueryTicketFactory(samlAttributeQueryTicketExpirationPolicy(),
                openSamlConfigBean,
                webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "samlAttributeQueryTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy samlAttributeQueryTicketExpirationPolicy() {
        return new SamlAttributeQueryTicketExpirationPolicy(casProperties.getTicket().getSt().getTimeToKillInSeconds());
    }
}
