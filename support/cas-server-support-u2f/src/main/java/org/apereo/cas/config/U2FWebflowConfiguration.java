package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.web.U2FRegisteredDevicesEndpoint;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountCheckRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountSaveRegistrationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.u2f.web.flow.U2FMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.u2f.web.flow.U2FMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartAuthenticationAction;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartRegistrationAction;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import com.yubico.u2f.U2F;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link U2FWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.U2F)
@AutoConfiguration
public class U2FWebflowConfiguration {

    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "U2FWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FWebflowRegistryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "u2fFlowRegistry")
        public FlowDefinitionRegistry u2fFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, U2FMultifactorWebflowConfigurer.MFA_U2F_EVENT_ID);
            return builder.build();
        }
    }

    @Configuration(value = "U2FWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "u2fMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer u2fMultifactorWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("u2fFlowRegistry")
            final FlowDefinitionRegistry u2fFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val cfg = new U2FMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, u2fFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }

    }

    @Configuration(value = "U2FWebflowEventConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FWebflowEventConfiguration {

        @ConditionalOnMissingBean(name = "u2fAuthenticationWebflowEventResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowEventResolver u2fAuthenticationWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext")
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
            return new U2FAuthenticationWebflowEventResolver(casWebflowConfigurationContext);
        }
    }

    @Configuration(value = "U2FWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FWebflowExecutionPlanConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "u2fCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer u2fCasWebflowExecutionPlanConfigurer(
            @Qualifier("u2fMultifactorWebflowConfigurer")
            final CasWebflowConfigurer u2fMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(u2fMultifactorWebflowConfigurer);
        }

    }

    @Configuration(value = "U2FWebflowEndpointConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FWebflowEndpointConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public U2FRegisteredDevicesEndpoint u2fRegisteredDevicesEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("u2fDeviceRepository")
            final ObjectProvider<U2FDeviceRepository> u2fDeviceRepository) {
            return new U2FRegisteredDevicesEndpoint(casProperties, u2fDeviceRepository);
        }
    }

    @Configuration(value = "U2FWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_U2F_AUTHENTICATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action u2fAuthenticationWebflowAction(
            @Qualifier("u2fAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver u2fAuthenticationWebflowEventResolver) {
            return new U2FAuthenticationWebflowAction(u2fAuthenticationWebflowEventResolver);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_U2F_START_AUTHENTICATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action u2fStartAuthenticationAction(final CasConfigurationProperties casProperties,
                                                   @Qualifier("u2fDeviceRepository")
                                                   final U2FDeviceRepository u2fDeviceRepository,
                                                   @Qualifier("u2fService")
                                                   final U2F u2fService) {
            return new U2FStartAuthenticationAction(u2fService, casProperties.getServer().getName(), u2fDeviceRepository);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_U2F_START_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action u2fStartRegistrationAction(final CasConfigurationProperties casProperties,
                                                 @Qualifier("u2fDeviceRepository")
                                                 final U2FDeviceRepository u2fDeviceRepository,
                                                 @Qualifier("u2fService")
                                                 final U2F u2fService) {
            return new U2FStartRegistrationAction(u2fService, casProperties.getServer().getName(), u2fDeviceRepository);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_U2F_CHECK_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action u2fCheckAccountRegistrationAction(
            @Qualifier("u2fDeviceRepository")
            final U2FDeviceRepository u2fDeviceRepository) {
            return new U2FAccountCheckRegistrationAction(u2fDeviceRepository);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_U2F_SAVE_REGISTRATION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action u2fSaveAccountRegistrationAction(
            @Qualifier("u2fDeviceRepository")
            final U2FDeviceRepository u2fDeviceRepository,
            @Qualifier("u2fService")
            final U2F u2fService) {
            return new U2FAccountSaveRegistrationAction(u2fService, u2fDeviceRepository);
        }
    }

    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "u2f")
    @Configuration(value = "U2fMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("u2fMultifactorWebflowConfigurer")
    public static class U2FMultifactorTrustConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.u2f.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "u2fMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer u2fMultifactorTrustWebflowConfigurer(
            @Qualifier("u2fFlowRegistry")
            final FlowDefinitionRegistry u2fFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new U2FMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                        loginFlowDefinitionRegistry, u2fFlowRegistry,
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
        public CasWebflowExecutionPlanConfigurer u2fMultifactorTrustCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("u2fMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer u2fMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(u2fMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
