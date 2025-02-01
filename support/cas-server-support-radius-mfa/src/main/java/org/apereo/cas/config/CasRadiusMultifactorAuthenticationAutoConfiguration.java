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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasRadiusMultifactorAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.RadiusMFA)
@AutoConfiguration
@Import({
    RadiusTokenAuthenticationComponentSerializationConfiguration.class,
    RadiusMultifactorConfiguration.class,
    RadiusTokenAuthenticationEventExecutionPlanConfiguration.class,
    RadiusTokenAuthenticationMultifactorProviderBypassConfiguration.class
})
public class CasRadiusMultifactorAuthenticationAutoConfiguration {

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "radius")
    @Configuration(value = "RadiusMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("radiusMultifactorWebflowConfigurer")
    public static class RadiusMultifactorTrustConfiguration {
        private static final int WEBFLOW_CONFIGURER_ORDER = 100;

        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.radius.trusted-device-enabled")
            .isTrue().evenIfMissing().and("cas.authn.mfa.radius.client.inet-address");

        @ConditionalOnMissingBean(name = "radiusMultifactorTrustConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer radiusMultifactorTrustConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusFlowRegistry")
            final FlowDefinitionRegistry radiusFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new BasicMultifactorTrustedWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, radiusFlowRegistry,
                        applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer radiusMultifactorTrustCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusMultifactorTrustConfigurer")
            final CasWebflowConfigurer radiusMultifactorTrustConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(radiusMultifactorTrustConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
