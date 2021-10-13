package org.apereo.cas.pm.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementCaptchaWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementSingleSignOnParticipationStrategy;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.HandlePasswordExpirationWarningMessagesAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordResetAction;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.pm.web.flow.actions.ValidatePasswordResetTokenAction;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestAction;
import org.apereo.cas.pm.web.flow.actions.VerifySecurityQuestionsAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;

/**
 * This is {@link PasswordManagementWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "passwordManagementWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class PasswordManagementWebflowConfiguration {
    
    @Configuration(value = "PasswordManagementWebflowSingleSignOnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementWebflowSingleSignOnConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementSingleSignOnParticipationStrategy")
        @Autowired
        public SingleSignOnParticipationStrategy passwordManagementSingleSignOnParticipationStrategy(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new PasswordManagementSingleSignOnParticipationStrategy(
                servicesManager, ticketRegistrySupport, authenticationServiceSelectionPlan, centralAuthenticationService);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementSingleSignOnParticipationStrategyConfigurer")
        public SingleSignOnParticipationStrategyConfigurer passwordManagementSingleSignOnParticipationStrategyConfigurer(
            @Qualifier("passwordManagementSingleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy passwordManagementSingleSignOnParticipationStrategy) {
            return chain -> chain.addStrategy(passwordManagementSingleSignOnParticipationStrategy);
        }
    }
    
    @Configuration(value = "PasswordManagementWebflowAdapterConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementWebflowAdapterConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public HandlerAdapter passwordResetHandlerAdapter(
            @Qualifier("loginFlowExecutor")
            final FlowExecutor loginFlowExecutor) {
            val handler = new FlowHandlerAdapter() {
                @Override
                public boolean supports(final Object handler) {
                    return super.supports(handler) && ((FlowHandler) handler)
                        .getFlowId().equals(PasswordManagementWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
                }
            };
            handler.setFlowExecutor(loginFlowExecutor);
            return handler;
        }
    }

    @Configuration(value = "PasswordManagementWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "passwordManagementWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer passwordManagementWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new PasswordManagementWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordManagementCasWebflowExecutionPlanConfigurer")
        @Autowired
        public CasWebflowExecutionPlanConfigurer passwordManagementCasWebflowExecutionPlanConfigurer(
            @Qualifier("passwordManagementWebflowConfigurer")
            final CasWebflowConfigurer passwordManagementWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(passwordManagementWebflowConfigurer);
        }
    }

    @Configuration(value = "PasswordManagementWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementWebflowActionsConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE)
        public Action initPasswordChangeAction(final CasConfigurationProperties casProperties) {
            return new InitPasswordChangeAction(casProperties);
        }

        @ConditionalOnMissingBean(name = "initPasswordResetAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public Action initPasswordResetAction(
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService) {
            return new InitPasswordResetAction(passwordManagementService);
        }

        @ConditionalOnMissingBean(name = "passwordChangeAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public Action passwordChangeAction(
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier("passwordValidationService")
            final PasswordValidationService passwordValidationService) {
            return new PasswordChangeAction(passwordManagementService, passwordValidationService);
        }

        @ConditionalOnMissingBean(name = "sendPasswordResetInstructionsAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action sendPasswordResetInstructionsAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("defaultPrincipalResolver")
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier("communicationsManager")
            final CommunicationsManager communicationsManager,
            @Qualifier("defaultTicketFactory")
            final TicketFactory ticketFactory) {
            return new SendPasswordResetInstructionsAction(casProperties, communicationsManager,
                passwordManagementService, ticketRegistry, ticketFactory, defaultPrincipalResolver);
        }

        @ConditionalOnMissingBean(name = "verifyPasswordResetRequestAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action verifyPasswordResetRequestAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new VerifyPasswordResetRequestAction(casProperties,
                passwordManagementService, centralAuthenticationService);
        }

        @ConditionalOnMissingBean(name = "handlePasswordExpirationWarningMessagesAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action handlePasswordExpirationWarningMessagesAction() {
            return new HandlePasswordExpirationWarningMessagesAction();
        }

        @ConditionalOnMissingBean(name = "verifySecurityQuestionsAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action verifySecurityQuestionsAction(final CasConfigurationProperties casProperties,
                                                    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
                                                    final PasswordManagementService passwordManagementService) {
            if (!casProperties.getAuthn().getPm().getReset().isSecurityQuestionsEnabled()) {
                LOGGER.debug("Functionality to handle security questions for password management is not enabled");
                return new StaticEventExecutionAction("success");
            }
            return new VerifySecurityQuestionsAction(passwordManagementService);
        }

        @ConditionalOnMissingBean(name = "validatePasswordResetTokenAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action validatePasswordResetTokenAction(
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new ValidatePasswordResetTokenAction(passwordManagementService, centralAuthenticationService);
        }

    }
    
    @ConditionalOnProperty(prefix = "cas.authn.pm.google-recaptcha", name = "enabled", havingValue = "true")
    @Configuration(value = "passwordManagementCaptchaConfiguration", proxyBeanMethods = false)
    public static class PasswordManagementCaptchaConfiguration {

        @ConditionalOnMissingBean(name = "passwordManagementCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public CasWebflowConfigurer passwordManagementCaptchaWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val configurer = new PasswordManagementCaptchaWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
            configurer.setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 1);
            return configurer;
        }

        @ConditionalOnMissingBean(name = "passwordResetValidateCaptchaAction")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public Action passwordResetValidateCaptchaAction(final CasConfigurationProperties casProperties) {
            val recaptcha = casProperties.getAuthn().getPm().getGoogleRecaptcha();
            return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha));
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT_CAPTCHA)
        public Action passwordResetInitializeCaptchaAction(final CasConfigurationProperties casProperties) {
            val recaptcha = casProperties.getAuthn().getPm().getGoogleRecaptcha();
            return new InitializeCaptchaAction(recaptcha) {
                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    WebUtils.putRecaptchaPasswordManagementEnabled(requestContext, recaptcha);
                    return super.doExecute(requestContext);
                }
            };
        }

        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "passwordManagementCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordManagementCaptchaWebflowExecutionPlanConfigurer(
            @Qualifier("passwordManagementCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return plan -> plan.registerWebflowConfigurer(cfg);
        }
    }
}
