package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
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
    private static final BeanCondition CONDITION = BeanCondition.on("cas.google-recaptcha.enabled").isTrue().evenIfMissing();

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
        return BeanSupplier.of(CasWebflowConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new CasCaptchaWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "captchaValidator")
    public CaptchaValidator captchaValidator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CaptchaValidator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> CaptchaValidator.getInstance(casProperties.getGoogleRecaptcha()))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA)
    public Action validateCaptchaAction(
        final CasConfigurationProperties casProperties,
        @Qualifier("captchaActivationStrategy")
        final CaptchaActivationStrategy captchaActivationStrategy,
        @Qualifier("captchaValidator")
        final CaptchaValidator captchaValidator,
        final ConfigurableApplicationContext applicationContext) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new ValidateCaptchaAction(captchaValidator, captchaActivationStrategy))
                .otherwiseProxy()
                .get())
            .withId(CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA)
            .build()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "captchaActivationStrategy")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CaptchaActivationStrategy captchaActivationStrategy(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return BeanSupplier.of(CaptchaActivationStrategy.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultCaptchaActivationStrategy(servicesManager))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_CAPTCHA)
    public Action initializeCaptchaAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("captchaActivationStrategy")
        final CaptchaActivationStrategy captchaActivationStrategy) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new InitializeCaptchaAction(captchaActivationStrategy,
                    requestContext -> requestContext.getFlowScope().put("recaptchaLoginEnabled",
                        casProperties.getGoogleRecaptcha().isEnabled()),
                    casProperties.getGoogleRecaptcha()))
                .otherwiseProxy()
                .get())
            .withId(CasWebflowConstants.ACTION_ID_INIT_CAPTCHA)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "captchaCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer captchaCasWebflowExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("captchaWebflowConfigurer")
        final CasWebflowConfigurer captchaWebflowConfigurer) {
        return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerWebflowConfigurer(captchaWebflowConfigurer))
            .otherwiseProxy()
            .get();
    }
}
