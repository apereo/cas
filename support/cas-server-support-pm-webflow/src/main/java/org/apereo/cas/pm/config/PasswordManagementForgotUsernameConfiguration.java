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
import org.apereo.cas.web.CaptchaValidator;
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
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordManagementForgotUsernameConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "PasswordManagementForgotUsernameConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PasswordManagementForgotUsernameConfiguration {

    @Configuration(value = "PasswordManagementForgotUsernameAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementForgotUsernameAuditConfiguration {
        @Bean
        @Autowired
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
        @Autowired
        public Action sendForgotUsernameInstructionsAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("communicationsManager")
            final CommunicationsManager communicationsManager,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier("defaultPrincipalResolver")
            final PrincipalResolver defaultPrincipalResolver) {
            return new SendForgotUsernameInstructionsAction(casProperties, communicationsManager,
                passwordManagementService, defaultPrincipalResolver);
        }

        @ConditionalOnMissingBean(name = "forgotUsernameWebflowConfigurer")
        @Bean
        @Autowired
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
        @Autowired
        @ConditionalOnMissingBean(name = "forgotUsernameCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer forgotUsernameCasWebflowExecutionPlanConfigurer(
            @Qualifier("forgotUsernameWebflowConfigurer")
            final CasWebflowConfigurer forgotUsernameWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(forgotUsernameWebflowConfigurer);
        }

    }

    @ConditionalOnProperty(prefix = "cas.authn.pm.forgot-username.google-recaptcha", name = "enabled", havingValue = "true")
    @Configuration(value = "forgotUsernameCaptchaConfiguration", proxyBeanMethods = false)
    public static class ForgotUsernameCaptchaConfiguration {

        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
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
        @Autowired
        public Action forgotUsernameValidateCaptchaAction(final CasConfigurationProperties casProperties) {
            val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
            return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha));
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA)
        @Autowired
        public Action forgotUsernameInitializeCaptchaAction(final CasConfigurationProperties casProperties) {
            val recaptcha = casProperties.getAuthn().getPm().getForgotUsername().getGoogleRecaptcha();
            return new InitializeCaptchaAction(recaptcha) {
                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    WebUtils.putRecaptchaForgotUsernameEnabled(requestContext, recaptcha);
                    return super.doExecute(requestContext);
                }
            };
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "forgotUsernameCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer forgotUsernameCaptchaWebflowExecutionPlanConfigurer(
            @Qualifier("forgotUsernameCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return plan -> plan.registerWebflowConfigurer(cfg);
        }
    }
}
