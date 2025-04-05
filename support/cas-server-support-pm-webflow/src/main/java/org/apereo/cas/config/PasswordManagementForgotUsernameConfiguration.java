package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.ForgotUsernameCaptchaWebflowConfigurer;
import org.apereo.cas.pm.web.flow.ForgotUsernameWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.SendForgotUsernameInstructionsAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.DefaultCaptchaActivationStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.SpringWebflowActionExecutionAuditablePrincipalResolver;
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
 * This is {@link PasswordManagementForgotUsernameConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ForgotUsername)
@Configuration(value = "PasswordManagementForgotUsernameConfiguration", proxyBeanMethods = false)
class PasswordManagementForgotUsernameConfiguration {

    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pm.forgot-username.enabled").isTrue().evenIfMissing();

    @Configuration(value = "PasswordManagementForgotUsernameAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementForgotUsernameAuditConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "forgotUsernameAuditTrailRecordResolutionPlanConfigurer")
        public AuditTrailRecordResolutionPlanConfigurer forgotUsernameAuditTrailRecordResolutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver) {
            return BeanSupplier.of(AuditTrailRecordResolutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    plan.registerAuditActionResolver(AuditActionResolvers.REQUEST_FORGOT_USERNAME_ACTION_RESOLVER, new DefaultAuditActionResolver());
                    plan.registerAuditResourceResolver(AuditResourceResolvers.REQUEST_FORGOT_USERNAME_RESOURCE_RESOLVER, returnValueResourceResolver);
                    plan.registerAuditPrincipalResolver(AuditPrincipalResolvers.REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER,
                        new SpringWebflowActionExecutionAuditablePrincipalResolver(SendForgotUsernameInstructionsAction.REQUEST_PARAMETER_EMAIL));
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "PasswordManagementForgotUsernameWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementForgotUsernameWebflowConfiguration {
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action sendForgotUsernameInstructionsAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new SendForgotUsernameInstructionsAction(casProperties, communicationsManager,
                    passwordManagementService, defaultPrincipalResolver, tenantExtractor))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @ConditionalOnMissingBean(name = "forgotUsernameWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer forgotUsernameWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new ForgotUsernameWebflowConfigurer(flowBuilderServices,
                    flowDefinitionRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "forgotUsernameCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer forgotUsernameCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("forgotUsernameWebflowConfigurer")
            final CasWebflowConfigurer forgotUsernameWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(forgotUsernameWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ForgotUsername, module = "captcha")
    @Configuration(value = "ForgotUsernameCaptchaConfiguration", proxyBeanMethods = false)
    static class ForgotUsernameCaptchaConfiguration {

        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pm.forgot-username.google-recaptcha.enabled").isTrue();

        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowConfigurer forgotUsernameCaptchaWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val configurer = new ForgotUsernameCaptchaWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry,
                        applicationContext, casProperties);
                    configurer.setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 2);
                    return configurer;
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_VALIDATE_CAPTCHA)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action forgotUsernameValidateCaptchaAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("forgotUsernameCaptchaActivationStrategy")
            final CaptchaActivationStrategy forgotUsernameCaptchaActivationStrategy) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
                    return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha), forgotUsernameCaptchaActivationStrategy);
                })
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA)
        public Action forgotUsernameInitializeCaptchaAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("forgotUsernameCaptchaActivationStrategy")
            final CaptchaActivationStrategy forgotUsernameCaptchaActivationStrategy,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
                    return new InitializeCaptchaAction(forgotUsernameCaptchaActivationStrategy,
                        requestContext -> WebUtils.putRecaptchaForgotUsernameEnabled(requestContext, recaptcha),
                        recaptcha);
                })
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaActivationStrategy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CaptchaActivationStrategy forgotUsernameCaptchaActivationStrategy(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return BeanSupplier.of(CaptchaActivationStrategy.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DefaultCaptchaActivationStrategy(servicesManager))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer forgotUsernameCaptchaWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("forgotUsernameCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(cfg))
                .otherwiseProxy()
                .get();
        }
    }
}
