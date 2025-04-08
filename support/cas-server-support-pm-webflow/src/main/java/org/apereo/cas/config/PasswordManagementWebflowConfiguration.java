package org.apereo.cas.config;

import org.apereo.cas.acct.AccountProfileServiceTicketGeneratorAuthority;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.pm.PasswordStrengthAuthenticationPostProcessor;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.web.PasswordManagementEndpoint;
import org.apereo.cas.pm.web.flow.PasswordManagementAccountProfileWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementCaptchaWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementMultifactorTrustWebflowConfigurer;
import org.apereo.cas.pm.web.flow.PasswordManagementSingleSignOnParticipationStrategy;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.pm.web.flow.WeakPasswordWebflowExceptionHandler;
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
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
import java.util.List;

/**
 * This is {@link PasswordManagementWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "PasswordManagementWebflowConfiguration", proxyBeanMethods = false)
class PasswordManagementWebflowConfiguration {

    @Configuration(value = "PasswordManagementWebflowSingleSignOnConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementWebflowSingleSignOnConfiguration {
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
    static class PasswordManagementWebflowAdapterConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "passwordResetHandlerAdapter")
        public HandlerAdapter passwordResetHandlerAdapter(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
            final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier("loginFlowExecutor")
            final FlowExecutor loginFlowExecutor) {
            val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_PASSWORD_RESET, webflowExecutionPlan);
            handler.setFlowExecutor(loginFlowExecutor);
            return handler;
        }
    }

    @Configuration(value = "PasswordManagementWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementWebflowBaseConfiguration {
        @ConditionalOnMissingBean(name = "passwordManagementWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer passwordManagementWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new PasswordManagementWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordManagementCasWebflowExecutionPlanConfigurer(
            @Qualifier("passwordManagementWebflowConfigurer")
            final CasWebflowConfigurer passwordManagementWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(passwordManagementWebflowConfigurer);
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordManagementMultifactorWebflowCustomizer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasMultifactorWebflowCustomizer passwordManagementMultifactorWebflowCustomizer() {
            return new CasMultifactorWebflowCustomizer() {
                @Override
                public List<String> getWebflowAttributeMappings() {
                    return List.of(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_QUERY,
                        CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_REQUEST,
                        CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT_BUILDER,
                        CasWebflowConstants.ATTRIBUTE_AUTHENTICATION,
                        "mfaDeviceRegistrationEnabled",
                        "multifactorTrustedDevicesDisabled");
                }
            };
        }
    }

    @Configuration(value = "PasswordManagementWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementWebflowActionsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_UNLOCK_PREPARE)
        public Action accountUnlockStatusPrepareAction(final ConfigurableApplicationContext applicationContext,
                                                       final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(AccountUnlockStatusPrepareAction::new)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_UNLOCK_PREPARE)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_UNLOCK_ACCOUNT_STATUS)
        public Action accountUnlockStatusAction(
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(() -> new AccountUnlockStatusAction(passwordManagementService))
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_UNLOCK_ACCOUNT_STATUS)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE)
        public Action initPasswordChangeAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {

            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(() -> new InitPasswordChangeAction(casProperties))
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action initPasswordResetAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(MultifactorAuthenticationProviderSelector.BEAN_NAME)
            final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(MultifactorAuthenticationContextValidator.BEAN_NAME)
            final MultifactorAuthenticationContextValidator multifactorAuthenticationContextValidator) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(() -> new InitPasswordResetAction(passwordManagementService,
                        casProperties, defaultPrincipalResolver,
                        multifactorAuthenticationProviderSelector, authenticationSystemSupport,
                        multifactorAuthenticationContextValidator))
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_CHANGE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Action passwordChangeAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(PasswordValidationService.BEAN_NAME)
            final PasswordValidationService passwordValidationService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(() -> new PasswordChangeAction(passwordManagementService, passwordValidationService))
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_PASSWORD_CHANGE)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_SEND_INSTRUCTIONS)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action sendPasswordResetInstructionsAction(
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationProviderSelector.BEAN_NAME)
            final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory,
            @Qualifier(PasswordResetUrlBuilder.BEAN_NAME)
            final PasswordResetUrlBuilder passwordResetUrlBuilder) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(() -> new SendPasswordResetInstructionsAction(casProperties, communicationsManager,
                        passwordManagementService, ticketRegistry, ticketFactory,
                        defaultPrincipalResolver, passwordResetUrlBuilder,
                        multifactorAuthenticationProviderSelector, authenticationSystemSupport, servicesManager))
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_SEND_INSTRUCTIONS)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_REQUEST)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action verifyPasswordResetRequestAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(() -> new VerifyPasswordResetRequestAction(casProperties,
                        passwordManagementService, ticketRegistrySupport))
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_REQUEST)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORD_EXPIRATION_HANDLE_WARNINGS)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action handlePasswordExpirationWarningMessagesAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(HandlePasswordExpirationWarningMessagesAction::new)
                .withId(CasWebflowConstants.ACTION_ID_PASSWORD_EXPIRATION_HANDLE_WARNINGS)
                .build()
                .get();
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
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .alwaysMatch()
                    .supply(() -> new ValidatePasswordResetTokenAction(passwordManagementService, ticketRegistry))
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VALIDATE_TOKEN)
                .build()
                .get();
        }
    }

    @Configuration(value = "PasswordManagementPolicyConfiguration", proxyBeanMethods = false)
    static class PasswordManagementPolicyConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pm.core.password-policy-pattern");

        @ConditionalOnMissingBean(name = "passwordStrengthAuthenticationPostProcessor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationPostProcessor passwordStrengthAuthenticationPostProcessor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordValidationService.BEAN_NAME)
            final PasswordValidationService passwordValidationService) {
            return BeanSupplier.of(AuthenticationPostProcessor.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new PasswordStrengthAuthenticationPostProcessor(passwordValidationService))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "passwordManagementAuthenticationExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer passwordManagementAuthenticationExecutionPlanConfigurer(
            @Qualifier("passwordStrengthAuthenticationPostProcessor")
            final AuthenticationPostProcessor passwordStrengthAuthenticationPostProcessor,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerAuthenticationPostProcessor(passwordStrengthAuthenticationPostProcessor))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "weakPasswordWebflowExceptionHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExceptionHandler weakPasswordWebflowExceptionHandler(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowExceptionHandler.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(WeakPasswordWebflowExceptionHandler::new)
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "PasswordManagementCaptchaConfiguration", proxyBeanMethods = false)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordManagement, module = "captcha")
    static class PasswordManagementCaptchaConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pm.google-recaptcha.enabled").isTrue();

        @ConditionalOnMissingBean(name = "passwordManagementCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowConfigurer passwordManagementCaptchaWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val configurer = new PasswordManagementCaptchaWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, applicationContext, casProperties);
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
    static class PasswordManagementAccountProfileConfiguration {

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
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new AccountProfileUpdateSecurityQuestionsAction(passwordManagementService, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_UPDATE_SECURITY_QUESTIONS)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_PASSWORD_CHANGE_REQUEST)
        public Action accountProfilePasswordChangeRequestAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(PasswordResetUrlBuilder.BEAN_NAME)
            final PasswordResetUrlBuilder passwordResetUrlBuilder) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new AccountProfilePasswordChangeRequestAction(ticketRegistry, passwordResetUrlBuilder))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_PASSWORD_CHANGE_REQUEST)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT)
        public Action prepareAccountProfilePasswordMgmtAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final PasswordManagementService passwordManagementService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new AccountProfilePreparePasswordManagementAction(passwordManagementService, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementAccountProfileWebflowConfigurer")
        @DependsOn("accountProfileWebflowConfigurer")
        public CasWebflowConfigurer passwordManagementAccountProfileWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new PasswordManagementAccountProfileWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
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

    @Configuration(value = "PasswordManagementEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class PasswordManagementEndpointsConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PasswordManagementEndpoint passwordManagementEndpoint(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
            final ObjectProvider<PasswordManagementService> passwordManagementService,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final ObjectProvider<PrincipalResolver> defaultPrincipalResolver,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final ObjectProvider<CommunicationsManager> communicationsManager,
            @Qualifier(PasswordResetUrlBuilder.BEAN_NAME)
            final ObjectProvider<PasswordResetUrlBuilder> passwordResetUrlBuilder,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer) {
            return new PasswordManagementEndpoint(casProperties, applicationContext,
                communicationsManager, passwordManagementService,
                passwordResetUrlBuilder, webApplicationServiceFactory,
                servicesManager, defaultPrincipalResolver,
                authenticationSystemSupport, registeredServiceAccessStrategyEnforcer);
        }
    }

    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "PasswordManagementMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("passwordManagementWebflowConfigurer")
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices)
    public static class PasswordManagementMultifactorTrustConfiguration {
        @ConditionalOnMissingBean(name = "passwordManagementMfaTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer passwordManagementMfaTrustWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new PasswordManagementMultifactorTrustWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordManagementMfaTrustWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordManagementMfaTrustWebflowExecutionPlanConfigurer(
            @Qualifier("passwordManagementMfaTrustWebflowConfigurer")
            final CasWebflowConfigurer passwordManagementMfaTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(passwordManagementMfaTrustWebflowConfigurer);
        }
    }
}
