package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorTrustedDeviceWebflowConfigurer;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasSimpleMultifactorAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@AutoConfiguration
@Import({
    CasSimpleMultifactorAuthenticationComponentSerializationConfiguration.class,
    CasSimpleMultifactorAuthenticationConfiguration.class,
    CasSimpleMultifactorAuthenticationEventExecutionPlanConfiguration.class,
    CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration.class,
    CasSimpleMultifactorAuthenticationRestConfiguration.class,
    CasSimpleMultifactorAuthenticationTicketCatalogConfiguration.class
})
public class CasSimpleMultifactorAuthenticationAutoConfiguration {

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "CasSimpleMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("casSimpleMultifactorAuthenticationTicketFactoryConfigurer")
    public static class CasSimpleMultifactorTrustConfiguration {
        private static final int WEBFLOW_CONFIGURER_ORDER = 100;
        
        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer mfaSimpleMultifactorTrustWebflowConfigurer(
            @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val cfg = new CasSimpleMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry,
                mfaSimpleAuthenticatorFlowRegistry,
                applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @ConditionalOnMissingBean(name = "casSimpleMultifactorTrustWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer casSimpleMultifactorTrustWebflowExecutionPlanConfigurer(
            @Qualifier("mfaSimpleMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer mfaSimpleMultifactorTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(mfaSimpleMultifactorTrustWebflowConfigurer);
        }
    }
}
