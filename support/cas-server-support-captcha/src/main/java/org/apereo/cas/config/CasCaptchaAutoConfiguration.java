package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.DefaultCaptchaActivationStrategy;
import org.apereo.cas.web.flow.CasCaptchaWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasCaptchaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CAPTCHA)
@AutoConfiguration
public class CasCaptchaAutoConfiguration {
    @ConditionalOnMissingBean(name = "captchaWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer captchaWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new CasCaptchaWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = CaptchaValidator.BEAN_NAME)
    public CaptchaValidator captchaValidator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return CaptchaValidator.getInstance(casProperties.getGoogleRecaptcha());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA)
    public Action validateCaptchaAction(
        final CasConfigurationProperties casProperties,
        @Qualifier(CaptchaActivationStrategy.BEAN_NAME)
        final CaptchaActivationStrategy captchaActivationStrategy,
        @Qualifier(CaptchaValidator.BEAN_NAME)
        final CaptchaValidator captchaValidator,
        final ConfigurableApplicationContext applicationContext) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new ValidateCaptchaAction(captchaValidator, captchaActivationStrategy))
            .withId(CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA)
            .build()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = CaptchaActivationStrategy.BEAN_NAME)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CaptchaActivationStrategy captchaActivationStrategy(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return new DefaultCaptchaActivationStrategy(servicesManager);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_CAPTCHA)
    public Action initializeCaptchaAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CaptchaActivationStrategy.BEAN_NAME)
        final CaptchaActivationStrategy captchaActivationStrategy) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new InitializeCaptchaAction(captchaActivationStrategy,
                ConsumerExecutionAction.NO_OP_CONSUMER, casProperties.getGoogleRecaptcha()))
            .withId(CasWebflowConstants.ACTION_ID_INIT_CAPTCHA)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "captchaCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer captchaCasWebflowExecutionPlanConfigurer(
        @Qualifier("captchaWebflowConfigurer")
        final CasWebflowConfigurer captchaWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(captchaWebflowConfigurer);
    }
}
