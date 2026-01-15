package org.apereo.cas.config;

import module java.base;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasGoogleAuthenticatorAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator)
@AutoConfiguration
@Import({
    GoogleAuthenticatorConfiguration.class,
    GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class,
    GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration.class,
    GoogleAuthenticatorAuthenticationComponentSerializationConfiguration.class,
    GoogleAuthenticatorRestConfiguration.class
})
public class CasGoogleAuthenticatorAutoConfiguration {
    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "GoogleAuthenticatorMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("googleAuthenticatorMultifactorWebflowConfigurer")
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "gauth")
    public static class GoogleAuthenticatorMultifactorTrustConfiguration {
        private static final int WEBFLOW_CONFIGURER_ORDER = 100;

        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.gauth.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "gauthMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer gauthMultifactorTrustWebflowConfigurer(
            @Qualifier("googleAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry googleAuthenticatorFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new BasicMultifactorTrustedWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, googleAuthenticatorFlowRegistry, applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer gauthMultifactorTrustCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("gauthMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer gauthMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(gauthMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
