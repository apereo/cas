package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.trusted.web.flow.BasicMultifactorTrustedWebflowConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasTwilioMultifactorAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "twilio")
@AutoConfiguration
@Import({
    CasTwilioMultifactorAuthenticationEventExecutionPlanConfiguration.class,
    CasTwilioMultifactorAuthenticationComponentSerializationConfiguration.class,
    CasTwilioMultifactorAuthenticationMultifactorProviderBypassConfiguration.class,
    CasTwilioMultifactorAuthenticationWebflowConfiguration.class
})
public class CasTwilioMultifactorAuthenticationAutoConfiguration {

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "CasTwilioMultifactorTrustConfiguration", proxyBeanMethods = false)
    public static class CasTwilioMultifactorTrustConfiguration {
        private static final int WEBFLOW_CONFIGURER_ORDER = 100;

        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.twilio.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "mfaTwilioMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer mfaTwilioMultifactorTrustWebflowConfigurer(
            @Qualifier("mfaTwilioAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry mfaTwilioAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {

            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new BasicMultifactorTrustedWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry,
                        mfaTwilioAuthenticatorFlowRegistry,
                        applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "casTwilioMultifactorTrustWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer casTwilioMultifactorTrustWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("mfaTwilioMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer mfaTwilioMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(mfaTwilioMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
