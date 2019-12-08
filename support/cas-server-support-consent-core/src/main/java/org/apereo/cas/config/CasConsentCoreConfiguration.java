package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.AttributeConsentReportEndpoint;
import org.apereo.cas.consent.AttributeReleaseConsentCipherExecutor;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.DefaultConsentDecisionBuilder;
import org.apereo.cas.consent.DefaultConsentEngine;
import org.apereo.cas.consent.GroovyConsentRepository;
import org.apereo.cas.consent.InMemoryConsentRepository;
import org.apereo.cas.consent.JsonConsentRepository;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasConsentCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConsentCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasConsentCoreConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationActionResolver")
    private ObjectProvider<AuditActionResolver> authenticationActionResolver;

    @Autowired
    @Qualifier("returnValueResourceResolver")
    private ObjectProvider<AuditResourceResolver> returnValueResourceResolver;

    @ConditionalOnMissingBean(name = "consentEngine")
    @Bean
    @RefreshScope
    public ConsentEngine consentEngine() {
        return new DefaultConsentEngine(consentRepository(), consentDecisionBuilder());
    }

    @ConditionalOnMissingBean(name = "consentCipherExecutor")
    @Bean
    @RefreshScope
    public CipherExecutor consentCipherExecutor() {
        val consent = casProperties.getConsent();
        val crypto = consent.getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, AttributeReleaseConsentCipherExecutor.class);
        }
        LOGGER.debug("Consent attributes stored by CAS are not signed/encrypted.");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "consentDecisionBuilder")
    @Bean
    @RefreshScope
    public ConsentDecisionBuilder consentDecisionBuilder() {
        return new DefaultConsentDecisionBuilder(consentCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "consentRepository")
    @Bean
    @RefreshScope
    public ConsentRepository consentRepository() {
        val location = casProperties.getConsent().getJson().getLocation();
        if (location != null) {
            LOGGER.warn("Storing consent records in [{}]. This MAY NOT be appropriate in production. "
                + "Consider choosing an alternative repository format for storing consent decisions", location);
            return new JsonConsentRepository(location);
        }

        val groovy = casProperties.getConsent().getGroovy().getLocation();
        if (groovy != null) {
            return new GroovyConsentRepository(groovy);
        }

        LOGGER.warn("Storing consent records in memory. This option is ONLY relevant for demos and testing purposes.");
        return new InMemoryConsentRepository();
    }

    @Bean
    public AuditTrailRecordResolutionPlanConfigurer casConsentAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditActionResolver("SAVE_CONSENT_ACTION_RESOLVER", authenticationActionResolver.getObject());
            plan.registerAuditResourceResolver("SAVE_CONSENT_RESOURCE_RESOLVER", returnValueResourceResolver.getObject());
        };
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public AttributeConsentReportEndpoint attributeConsentReportEndpoint() {
        return new AttributeConsentReportEndpoint(casProperties, consentRepository(), consentEngine());
    }
}
