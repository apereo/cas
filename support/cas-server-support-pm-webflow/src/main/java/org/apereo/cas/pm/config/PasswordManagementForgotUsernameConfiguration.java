package org.apereo.cas.pm.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditPrincipalResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.ForgotUsernameCaptchaWebflowConfigurer;
import org.apereo.cas.pm.web.flow.ForgotUsernameWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.SendForgotUsernameInstructionsAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.DefaultCaptchaActivationStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.SpringWebflowActionExecutionAuditablePrincipalResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Configuration(value = "PasswordManagementForgotUsernameConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.pm.forgot-username", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PasswordManagementForgotUsernameConfiguration {

    @Configuration(value = "PasswordManagementForgotUsernameAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementForgotUsernameAuditConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "forgotUsernameAuditTrailRecordResolutionPlanConfigurer")
        public AuditTrailRecordResolutionPlanConfigurer forgotUsernameAuditTrailRecordResolutionPlanConfigurer(
            @Qualifier("returnValueResourceResolver")
            final AuditResourceResolver returnValueResourceResolver) {
            return plan -> {
                plan.registerAuditActionResolver(AuditActionResolvers.REQUEST_FORGOT_USERNAME_ACTION_RESOLVER,
                    new DefaultAuditActionResolver());
                plan.registerAuditResourceResolver(AuditResourceResolvers.REQUEST_FORGOT_USERNAME_RESOURCE_RESOLVER,
                    returnValueResourceResolver);
                plan.registerAuditPrincipalResolver(AuditPrincipalResolvers.REQUEST_FORGOT_USERNAME_PRINCIPAL_RESOLVER,
                    new SpringWebflowActionExecutionAuditablePrincipalResolver(SendForgotUsernameInstructionsAction.REQUEST_PARAMETER_EMAIL));
            };
        }
    }

    @Configuration(value = "PasswordManagementForgotUsernameWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementForgotUsernameWebflowConfiguration {
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action sendForgotUsernameInstructionsAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return new SendForgotUsernameInstructionsAction(casProperties, communicationsManager,
                passwordManagementService, defaultPrincipalResolver);
        }

        @ConditionalOnMissingBean(name = "forgotUsernameWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer forgotUsernameWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new ForgotUsernameWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "forgotUsernameCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer forgotUsernameCasWebflowExecutionPlanConfigurer(
            @Qualifier("forgotUsernameWebflowConfigurer")
            final CasWebflowConfigurer forgotUsernameWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(forgotUsernameWebflowConfigurer);
        }

    }

    @ConditionalOnProperty(prefix = "cas.authn.pm.forgot-username.google-recaptcha", name = "enabled", havingValue = "true")
    @Configuration(value = "ForgotUsernameCaptchaConfiguration", proxyBeanMethods = false)
    public static class ForgotUsernameCaptchaConfiguration {

        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowConfigurer forgotUsernameCaptchaWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val configurer = new ForgotUsernameCaptchaWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties);
            configurer.setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 2);
            return configurer;
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_VALIDATE_CAPTCHA)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action forgotUsernameValidateCaptchaAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("forgotUsernameCaptchaActivationStrategy")
            final CaptchaActivationStrategy forgotUsernameCaptchaActivationStrategy) {
            val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
            return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha), forgotUsernameCaptchaActivationStrategy);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA)
        public Action forgotUsernameInitializeCaptchaAction(
            @Qualifier("forgotUsernameCaptchaActivationStrategy")
            final CaptchaActivationStrategy forgotUsernameCaptchaActivationStrategy,
            final CasConfigurationProperties casProperties) {
            val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
            return new InitializeCaptchaAction(forgotUsernameCaptchaActivationStrategy,
                requestContext -> WebUtils.putRecaptchaForgotUsernameEnabled(requestContext, recaptcha),
                recaptcha);
        }

        @Bean
        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaActivationStrategy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CaptchaActivationStrategy forgotUsernameCaptchaActivationStrategy(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new DefaultCaptchaActivationStrategy(servicesManager);
        }

        @Bean
        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer forgotUsernameCaptchaWebflowExecutionPlanConfigurer(
            @Qualifier("forgotUsernameCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return plan -> plan.registerWebflowConfigurer(cfg);
        }
    }
}
