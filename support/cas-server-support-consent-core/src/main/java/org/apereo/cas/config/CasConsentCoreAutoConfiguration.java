package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.consent.AttributeConsentReportEndpoint;
import org.apereo.cas.consent.AttributeReleaseConsentCipherExecutor;
import org.apereo.cas.consent.ConsentActivationStrategy;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.consent.DefaultConsentActivationStrategy;
import org.apereo.cas.consent.DefaultConsentDecisionBuilder;
import org.apereo.cas.consent.DefaultConsentEngine;
import org.apereo.cas.consent.GroovyConsentActivationStrategy;
import org.apereo.cas.consent.GroovyConsentRepository;
import org.apereo.cas.consent.InMemoryConsentRepository;
import org.apereo.cas.consent.JsonConsentRepository;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.List;

/**
 * This is {@link CasConsentCoreAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Consent)
@AutoConfiguration
public class CasConsentCoreAutoConfiguration {
    @Configuration(value = "CasConsentCoreEngineConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentCoreEngineConfiguration {

        @ConditionalOnMissingBean(name = ConsentEngine.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConsentEngine consentEngine(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ConsentDecisionBuilder.BEAN_NAME)
            final ConsentDecisionBuilder consentDecisionBuilder,
            final List<ConsentableAttributeBuilder> builders,
            @Qualifier(ConsentRepository.BEAN_NAME)
            final ConsentRepository consentRepository) {
            AnnotationAwareOrderComparator.sortIfNecessary(builders);
            return new DefaultConsentEngine(consentRepository, consentDecisionBuilder,
                casProperties, builders, applicationContext);
        }
    }

    @Configuration(value = "CasConsentCoreBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentCoreBuilderConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "defaultConsentableAttributeBuilder")
        public ConsentableAttributeBuilder defaultConsentableAttributeBuilder() {
            return ConsentableAttributeBuilder.noOp();
        }

        @ConditionalOnMissingBean(name = "consentCipherExecutor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CipherExecutor consentCipherExecutor(final CasConfigurationProperties casProperties) {
            val consent = casProperties.getConsent().getCore();
            val crypto = consent.getCrypto();
            if (crypto.isEnabled()) {
                return CipherExecutorUtils.newStringCipherExecutor(crypto, AttributeReleaseConsentCipherExecutor.class);
            }
            LOGGER.debug("Consent attributes stored by CAS are not signed/encrypted.");
            return CipherExecutor.noOp();
        }

        @ConditionalOnMissingBean(name = ConsentDecisionBuilder.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConsentDecisionBuilder consentDecisionBuilder(
            @Qualifier("consentCipherExecutor")
            final CipherExecutor consentCipherExecutor) {
            return new DefaultConsentDecisionBuilder(consentCipherExecutor);
        }
    }

    @Configuration(value = "CasConsentCoreActivationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentCoreActivationConfiguration {
        @ConditionalOnMissingBean(name = ConsentActivationStrategy.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConsentActivationStrategy consentActivationStrategy(
            @Qualifier(ConsentEngine.BEAN_NAME)
            final ConsentEngine consentEngine,
            final CasConfigurationProperties casProperties) {
            val location = casProperties.getConsent().getActivationStrategyGroovyScript().getLocation();
            if (location != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
                return new GroovyConsentActivationStrategy(location, consentEngine, casProperties);
            }
            return new DefaultConsentActivationStrategy(consentEngine, casProperties);
        }

    }

    @Configuration(value = "CasConsentCoreRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentCoreRepositoryConfiguration {
        @ConditionalOnMissingBean(name = ConsentRepository.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConsentRepository consentRepository(final CasConfigurationProperties casProperties) {
            return FunctionUtils.doUnchecked(() -> {
                val location = casProperties.getConsent().getJson().getLocation();
                if (location != null) {
                    LOGGER.warn("Storing consent records in [{}]. This MAY NOT be appropriate in production. "
                        + "Consider choosing an alternative repository format for storing consent decisions", location);
                    return new JsonConsentRepository(location);
                }

                val groovy = casProperties.getConsent().getGroovy().getLocation();
                if (groovy != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
                    return new GroovyConsentRepository(groovy);
                }

                LOGGER.warn("Storing consent records in memory. This option is ONLY relevant for demos and testing purposes.");
                return new InMemoryConsentRepository();
            });
        }
    }

    @Configuration(value = "CasConsentCoreAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentCoreAuditConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casConsentAuditTrailRecordResolutionPlanConfigurer")
        public AuditTrailRecordResolutionPlanConfigurer casConsentAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("authenticationActionResolver")
            final AuditActionResolver authenticationActionResolver,
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver) {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.SAVE_CONSENT_ACTION_RESOLVER, authenticationActionResolver);
                plan.registerAuditActionResolver(AuditActionResolvers.VERIFY_CONSENT_ACTION_RESOLVER, authenticationActionResolver);

                plan.registerAuditResourceResolver(AuditResourceResolvers.SAVE_CONSENT_RESOURCE_RESOLVER, returnValueResourceResolver);
                plan.registerAuditResourceResolver(AuditResourceResolvers.VERIFY_CONSENT_RESOURCE_RESOLVER, returnValueResourceResolver);
            };
        }
    }

    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Configuration(value = "CasConsentCoreWebConfiguration", proxyBeanMethods = false)
    static class CasConsentCoreWebConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint
        public AttributeConsentReportEndpoint attributeConsentReportEndpoint(
            @Qualifier(ConsentEngine.BEAN_NAME)
            final ObjectProvider<ConsentEngine> consentEngine,
            @Qualifier(ConsentRepository.BEAN_NAME)
            final ObjectProvider<ConsentRepository> consentRepository,
            final CasConfigurationProperties casProperties) {
            return new AttributeConsentReportEndpoint(casProperties, consentRepository, consentEngine);
        }
    }
}
