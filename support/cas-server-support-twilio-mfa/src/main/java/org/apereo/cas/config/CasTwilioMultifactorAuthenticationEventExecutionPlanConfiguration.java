package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.twilio.CasDefaultTwilioMultifactorAuthenticationService;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorAuthenticationHandler;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasTwilioMultifactorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "twilio")
@Configuration(value = "CasTwilioMultifactorAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class CasTwilioMultifactorAuthenticationEventExecutionPlanConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasTwilioMultifactorAuthenticationService.BEAN_NAME)
    public CasTwilioMultifactorAuthenticationService casTwilioMultifactorAuthenticationService(
        final CasConfigurationProperties casProperties) {
        val props = casProperties.getAuthn().getMfa().getTwilio();
        return new CasDefaultTwilioMultifactorAuthenticationService(props);
    }

    @ConditionalOnMissingBean(name = "casTwilioMultifactorAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationHandler casTwilioMultifactorAuthenticationHandler(
        @Qualifier(CasTwilioMultifactorAuthenticationService.BEAN_NAME)
        final CasTwilioMultifactorAuthenticationService casTwilioMultifactorAuthenticationService,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("casTwilioMultifactorAuthenticationProvider")
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
        @Qualifier("casTwilioMultifactorPrincipalFactory")
        final PrincipalFactory casTwilioMultifactorPrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        val props = casProperties.getAuthn().getMfa().getTwilio();
        return new CasTwilioMultifactorAuthenticationHandler(casTwilioMultifactorAuthenticationService, props,
            applicationContext, casTwilioMultifactorPrincipalFactory,
            multifactorAuthenticationProvider);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casTwilioMultifactorAuthenticationProvider")
    public MultifactorAuthenticationProvider casTwilioMultifactorAuthenticationProvider(
        @Qualifier("casTwilioMultifactorBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator casTwilioMultifactorBypassEvaluator,
        @Qualifier("failureModeEvaluator")
        final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator,
        final CasConfigurationProperties casProperties) {
        val twilio = casProperties.getAuthn().getMfa().getTwilio();
        val provider = new CasTwilioMultifactorAuthenticationProvider();
        provider.setBypassEvaluator(casTwilioMultifactorBypassEvaluator);
        provider.setFailureMode(twilio.getFailureMode());
        provider.setFailureModeEvaluator(failureModeEvaluator);
        provider.setOrder(twilio.getRank());
        provider.setId(twilio.getId());
        return provider;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casTwilioMultifactorAuthenticationMetaDataPopulator")
    public AuthenticationMetaDataPopulator casTwilioMultifactorAuthenticationMetaDataPopulator(
        @Qualifier("casTwilioMultifactorAuthenticationProvider")
        final MultifactorAuthenticationProvider casTwilioMultifactorAuthenticationProvider,
        @Qualifier("casTwilioMultifactorAuthenticationHandler")
        final AuthenticationHandler casTwilioMultifactorAuthenticationHandler,
        final CasConfigurationProperties casProperties) {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
            casTwilioMultifactorAuthenticationHandler,
            casTwilioMultifactorAuthenticationProvider.getId()
        );
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casTwilioMultifactorProviderAuthenticationMetadataPopulator")
    public AuthenticationMetaDataPopulator casTwilioMultifactorProviderAuthenticationMetadataPopulator(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier("casTwilioMultifactorAuthenticationProvider")
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
        return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
            multifactorAuthenticationProvider, servicesManager);
    }

    @ConditionalOnMissingBean(name = "casTwilioMultifactorPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory casTwilioMultifactorPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "casTwilioMultifactorAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer casTwilioMultifactorAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("casTwilioMultifactorProviderAuthenticationMetadataPopulator")
        final AuthenticationMetaDataPopulator casTwilioMultifactorProviderAuthenticationMetadataPopulator,
        @Qualifier("casTwilioMultifactorAuthenticationHandler")
        final AuthenticationHandler casTwilioMultifactorAuthenticationHandler,
        @Qualifier("casTwilioMultifactorAuthenticationMetaDataPopulator")
        final AuthenticationMetaDataPopulator casTwilioMultifactorAuthenticationMetaDataPopulator) {
        return plan -> {
            plan.registerAuthenticationHandler(casTwilioMultifactorAuthenticationHandler);
            plan.registerAuthenticationMetadataPopulator(casTwilioMultifactorAuthenticationMetaDataPopulator);
            plan.registerAuthenticationMetadataPopulator(casTwilioMultifactorProviderAuthenticationMetadataPopulator);
        };
    }
}
