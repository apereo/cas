package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.DefaultCaptchaActivationStrategy;
import org.apereo.cas.web.flow.CasCaptchaWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasCaptchaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "CasCaptchaConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.CAPTCHA)
public class CasCaptchaConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.google-recaptcha.enabled").isTrue().evenIfMissing();

    @ConditionalOnMissingBean(name = "captchaWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer captchaWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) throws Exception {
        return BeanSupplier.of(CasWebflowConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new CasCaptchaWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "captchaValidator")
    public CaptchaValidator captchaValidator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(CaptchaValidator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> CaptchaValidator.getInstance(casProperties.getGoogleRecaptcha()))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "validateCaptchaAction")
    public Action validateCaptchaAction(
        @Qualifier("captchaActivationStrategy")
        final CaptchaActivationStrategy captchaActivationStrategy,
        @Qualifier("captchaValidator")
        final CaptchaValidator captchaValidator,
        final ConfigurableApplicationContext applicationContext) throws Exception {
        return BeanSupplier.of(Action.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new ValidateCaptchaAction(captchaValidator, captchaActivationStrategy))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "captchaActivationStrategy")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CaptchaActivationStrategy captchaActivationStrategy(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) throws Exception {
        return BeanSupplier.of(CaptchaActivationStrategy.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultCaptchaActivationStrategy(servicesManager))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "initializeCaptchaAction")
    public Action initializeCaptchaAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("captchaActivationStrategy")
        final CaptchaActivationStrategy captchaActivationStrategy) throws Exception {
        return BeanSupplier.of(Action.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new InitializeCaptchaAction(captchaActivationStrategy,
                requestContext -> requestContext.getFlowScope().put("recaptchaLoginEnabled", casProperties.getGoogleRecaptcha().isEnabled()),
                casProperties.getGoogleRecaptcha()))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "captchaCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer captchaCasWebflowExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("captchaWebflowConfigurer")
        final CasWebflowConfigurer captchaWebflowConfigurer) throws Exception {
        return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerWebflowConfigurer(captchaWebflowConfigurer))
            .otherwiseProxy()
            .get();
    }
}
