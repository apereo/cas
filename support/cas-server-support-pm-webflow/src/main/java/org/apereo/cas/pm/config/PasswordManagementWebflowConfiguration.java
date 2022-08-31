package org.apereo.cas.pm.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.acct.AccountProfileServiceTicketGeneratorAuthority;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.flow.PasswordManagementAccountProfileWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementCaptchaWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementSingleSignOnParticipationStrategy;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.actions.AccountProfilePasswordChangeRequestAction;
import org.apereo.cas.pm.web.flow.actions.AccountProfilePreparePasswordManagementAction;
import org.apereo.cas.pm.web.flow.actions.AccountProfileUpdateSecurityQuestionsAction;
import org.apereo.cas.pm.web.flow.actions.AccountUnlockStatusAction;
import org.apereo.cas.pm.web.flow.actions.AccountUnlockStatusPrepareAction;
import org.apereo.cas.pm.web.flow.actions.HandlePasswordExpirationWarningMessagesAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.InitPasswordResetAction;
import org.apereo.cas.pm.web.flow.actions.PasswordChangeAction;
import org.apereo.cas.pm.web.flow.actions.SendPasswordResetInstructionsAction;
import org.apereo.cas.pm.web.flow.actions.ValidatePasswordResetTokenAction;
import org.apereo.cas.pm.web.flow.actions.VerifyPasswordResetRequestAction;
import org.apereo.cas.pm.web.flow.actions.VerifySecurityQuestionsAction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
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
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;

/**
 * This is {@link PasswordManagementWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@AutoConfiguration
public class PasswordManagementWebflowConfiguration {

    @Configuration(value = "PasswordManagementWebflowSingleSignOnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementWebflowSingleSignOnConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementSingleSignOnParticipationStrategy")
        public SingleSignOnParticipationStrategy passwordManagementSingleSignOnParticipationStrategy(
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new PasswordManagementSingleSignOnParticipationStrategy(
                servicesManager, ticketRegistrySupport, authenticationServiceSelectionPlan);
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
        public HandlerAdapter passwordResetHandlerAdapter(
            @Qualifier("loginFlowExecutor")
            final FlowExecutor loginFlowExecutor) {
            val handler = new FlowHandlerAdapter() {
                @Override
                public boolean supports(final Object handler) {
                    return super.supports(handler) && ((FlowHandler) handler)
                        .getFlowId().equals(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordManagementCasWebflowExecutionPlanConfigurer(
            @Qualifier("passwordManagementWebflowConfigurer")
            final CasWebflowConfigurer passwordManagementWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(passwordManagementWebflowConfigurer);
        }
    }

    @Configuration(value = "PasswordManagementWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class PasswordManagementWebflowActionsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_UNLOCK_PREPARE)
        public Action accountUnlockStatusPrepareAction() {
            return new AccountUnlockStatusPrepareAction();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_UNLOCK_ACCOUNT_STATUS)
        public Action accountUnlockStatusAction(@Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
                                                final PasswordManagementService passwordManagementService) {
            return new AccountUnlockStatusAction(passwordManagementService);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE)
        public Action initPasswordChangeAction(final CasConfigurationProperties casProperties) {
            return new InitPasswordChangeAction(casProperties);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action initPasswordResetAction(
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService) {
            return new InitPasswordResetAction(passwordManagementService);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_CHANGE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action passwordChangeAction(
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier("passwordValidationService")
            final PasswordValidationService passwordValidationService) {
            return new PasswordChangeAction(passwordManagementService, passwordValidationService);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_SEND_INSTRUCTIONS)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action sendPasswordResetInstructionsAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(PasswordResetUrlBuilder.BEAN_NAME)
            final PasswordResetUrlBuilder passwordResetUrlBuilder) {
            return new SendPasswordResetInstructionsAction(casProperties, communicationsManager,
                passwordManagementService, ticketRegistry, ticketFactory,
                defaultPrincipalResolver, passwordResetUrlBuilder);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_REQUEST)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action verifyPasswordResetRequestAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new VerifyPasswordResetRequestAction(casProperties,
                passwordManagementService, ticketRegistry);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_EXPIRATION_HANDLE_WARNINGS)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action handlePasswordExpirationWarningMessagesAction() {
            return new HandlePasswordExpirationWarningMessagesAction();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_SECURITY_QUESTIONS)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action verifySecurityQuestionsAction(final CasConfigurationProperties casProperties,
                                                    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
                                                    final PasswordManagementService passwordManagementService) {
            if (!casProperties.getAuthn().getPm().getReset().isSecurityQuestionsEnabled()) {
                LOGGER.debug("Functionality to handle security questions for password management is not enabled");
                return new StaticEventExecutionAction("success");
            }
            return new VerifySecurityQuestionsAction(passwordManagementService);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VALIDATE_TOKEN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action validatePasswordResetTokenAction(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService) {
            return new ValidatePasswordResetTokenAction(passwordManagementService, ticketRegistry);
        }

    }

    @Configuration(value = "PasswordManagementCaptchaConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagement, module = "captcha")
    public static class PasswordManagementCaptchaConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pm.google-recaptcha.enabled").isTrue();

        @ConditionalOnMissingBean(name = "passwordManagementCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowConfigurer passwordManagementCaptchaWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val configurer = new PasswordManagementCaptchaWebflowConfigurer(flowBuilderServices,
                        loginFlowDefinitionRegistry, applicationContext, casProperties);
                    configurer.setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 1);
                    return configurer;
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VALIDATE_CAPTCHA)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action passwordResetValidateCaptchaAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordResetCaptchaActivationStrategy")
            final CaptchaActivationStrategy passwordResetCaptchaActivationStrategy) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val recaptcha = casProperties.getAuthn().getPm().getGoogleRecaptcha();
                    return new ValidateCaptchaAction(CaptchaValidator.getInstance(recaptcha), passwordResetCaptchaActivationStrategy);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordResetCaptchaActivationStrategy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CaptchaActivationStrategy passwordResetCaptchaActivationStrategy(
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
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT_CAPTCHA)
        public Action passwordResetInitializeCaptchaAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("passwordResetCaptchaActivationStrategy")
            final CaptchaActivationStrategy passwordResetCaptchaActivationStrategy,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val recaptcha = casProperties.getAuthn().getPm().getGoogleRecaptcha();
                    return new InitializeCaptchaAction(passwordResetCaptchaActivationStrategy,
                        requestContext -> WebUtils.putRecaptchaPasswordManagementEnabled(requestContext, recaptcha),
                        recaptcha);
                })
                .otherwiseProxy()
                .get();

        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordManagementCaptchaWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("passwordManagementCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(cfg))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "PasswordManagementAccountProfileConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    public static class PasswordManagementAccountProfileConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "accountProfileServiceTicketGeneratorAuthority")
        public ServiceTicketGeneratorAuthority accountProfileServiceTicketGeneratorAuthority(
            final CasConfigurationProperties casProperties) {
            return new AccountProfileServiceTicketGeneratorAuthority(casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_UPDATE_SECURITY_QUESTIONS)
        public Action accountProfileUpdateSecurityQuestionsAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService) {
            return new AccountProfileUpdateSecurityQuestionsAction(passwordManagementService, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_PASSWORD_CHANGE_REQUEST)
        public Action accountProfilePasswordChangeRequestAction(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PasswordResetUrlBuilder.BEAN_NAME)
            final PasswordResetUrlBuilder passwordResetUrlBuilder) {
            return new AccountProfilePasswordChangeRequestAction(ticketRegistry, passwordResetUrlBuilder);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT)
        public Action prepareAccountProfilePasswordMgmtAction(
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService) {
            return new AccountProfilePreparePasswordManagementAction(passwordManagementService, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementAccountProfileWebflowConfigurer")
        @DependsOn("accountProfileWebflowConfigurer")
        public CasWebflowConfigurer passwordManagementAccountProfileWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry accountProfileFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new PasswordManagementAccountProfileWebflowConfigurer(flowBuilderServices,
                accountProfileFlowRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementAccountProfileWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordManagementAccountProfileWebflowExecutionPlanConfigurer(
            @Qualifier("passwordManagementAccountProfileWebflowConfigurer")
            final CasWebflowConfigurer passwordManagementAccountProfileWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(passwordManagementAccountProfileWebflowConfigurer);
        }
    }
}
