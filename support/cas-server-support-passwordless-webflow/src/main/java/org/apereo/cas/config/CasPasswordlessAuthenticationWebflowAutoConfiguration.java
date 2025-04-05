package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.CaptchaValidator;
import org.apereo.cas.web.DefaultCaptchaActivationStrategy;
import org.apereo.cas.web.flow.AcceptPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.AcceptPasswordlessSelectionMenuAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.CreatePasswordlessAuthenticationTokenAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.DelegatedClientWebflowCustomizer;
import org.apereo.cas.web.flow.DetermineMultifactorPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.InitializeCaptchaAction;
import org.apereo.cas.web.flow.PasswordlessAuthenticationCaptchaWebflowConfigurer;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PasswordlessCasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.flow.PrepareForPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.PreparePasswordlessSelectionMenuAction;
import org.apereo.cas.web.flow.ValidateCaptchaAction;
import org.apereo.cas.web.flow.VerifyPasswordlessAccountAuthenticationAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.delegation.PasswordlessDelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.flow.delegation.PasswordlessDelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.flow.delegation.PasswordlessDetermineDelegatedAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
import java.util.List;

/**
 * This is {@link CasPasswordlessAuthenticationWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn)
@AutoConfiguration
public class CasPasswordlessAuthenticationWebflowAutoConfiguration {

    @Configuration(value = "PasswordlessCoreWebflowAuthentication", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn)
    static class PasswordlessCoreWebflowAuthentication {
        @Bean
        @ConditionalOnMissingBean(name = PasswordlessRequestParser.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PasswordlessRequestParser passwordlessRequestParser() {
            return PasswordlessRequestParser.defaultParser();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action verifyPasswordlessAccountAuthenticationAction(
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            @Qualifier(PasswordlessRequestParser.BEAN_NAME)
            final PasswordlessRequestParser passwordlessRequestParser,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
            final PasswordlessUserAccountStore passwordlessUserAccountStore) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new VerifyPasswordlessAccountAuthenticationAction(casProperties,
                    multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport,
                    passwordlessUserAccountStore, passwordlessRequestParser))
                .withId(CasWebflowConstants.ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_MULTIFACTOR_AUTHN)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action determineMultifactorPasswordlessAuthenticationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DetermineMultifactorPasswordlessAuthenticationAction(
                    casProperties, multifactorTriggerSelectionStrategy,
                    passwordlessPrincipalFactory, authenticationSystemSupport))
                .withId(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_MULTIFACTOR_AUTHN)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action acceptPasswordlessAuthenticationAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
            final PasswordlessUserAccountStore passwordlessUserAccountStore,
            @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
            final PasswordlessTokenRepository passwordlessTokenRepository,
            @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new AcceptPasswordlessAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                    serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
                    passwordlessTokenRepository, authenticationSystemSupport, passwordlessUserAccountStore))
                .withId(CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORDLESS_ACCEPT_SELECTION_MENU)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action acceptPasswordlessSelectionMenuAction(
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
            final PasswordlessUserAccountStore passwordlessUserAccountStore) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new AcceptPasswordlessSelectionMenuAction(casProperties,
                    passwordlessUserAccountStore, multifactorTriggerSelectionStrategy,
                    passwordlessPrincipalFactory, authenticationSystemSupport))
                .withId(CasWebflowConstants.ACTION_ID_PASSWORDLESS_ACCEPT_SELECTION_MENU)
                .build()
                .get();
        }


        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_SELECTION_MENU)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action preparePasswordlessSelectionMenuAction(
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
            final ObjectProvider<DelegatedClientIdentityProviderConfigurationProducer> delegatedClientIdentityProviderConfigurationProducer,
            @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
            final PasswordlessUserAccountStore passwordlessUserAccountStore) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new PreparePasswordlessSelectionMenuAction(casProperties,
                    multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport,
                    passwordlessUserAccountStore, delegatedClientIdentityProviderConfigurationProducer, communicationsManager))
                .withId(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_SELECTION_MENU)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DISPLAY_BEFORE_PASSWORDLESS_AUTHN)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action displayBeforePasswordlessAuthenticationAction(
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            @Qualifier(PasswordlessRequestParser.BEAN_NAME)
            final PasswordlessRequestParser passwordlessRequestParser,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
            final PasswordlessUserAccountStore passwordlessUserAccountStore) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new DisplayBeforePasswordlessAuthenticationAction(
                    casProperties, passwordlessUserAccountStore, passwordlessRequestParser,
                    multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport))
                .withId(CasWebflowConstants.ACTION_ID_DISPLAY_BEFORE_PASSWORDLESS_AUTHN)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CREATE_PASSWORDLESS_AUTHN_TOKEN)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action createPasswordlessAuthenticationTokenAction(
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            @Qualifier(PasswordlessRequestParser.BEAN_NAME)
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
            final PasswordlessTokenRepository passwordlessTokenRepository) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new CreatePasswordlessAuthenticationTokenAction(
                    casProperties, passwordlessTokenRepository, communicationsManager,
                    multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory,
                    authenticationSystemSupport, tenantExtractor))
                .withId(CasWebflowConstants.ACTION_ID_CREATE_PASSWORDLESS_AUTHN_TOKEN)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_LOGIN)
        public Action passwordlessPrepareLoginAction(
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new PrepareForPasswordlessAuthenticationAction(casProperties,
                    multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport))
                .withId(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_LOGIN)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = "passwordlessAuthenticationWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer passwordlessAuthenticationWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new PasswordlessAuthenticationWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @ConditionalOnMissingBean(name = "passwordlessCasWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer passwordlessCasWebflowExecutionPlanConfigurer(
            @Qualifier("passwordlessAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer passwordlessAuthenticationWebflowConfigurer,
            @Qualifier("passwordlessCasWebflowLoginContextProvider")
            final CasWebflowLoginContextProvider passwordlessCasWebflowLoginContextProvider) {
            return plan -> {
                plan.registerWebflowConfigurer(passwordlessAuthenticationWebflowConfigurer);
                plan.registerWebflowLoginContextProvider(passwordlessCasWebflowLoginContextProvider);
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordlessCasWebflowLoginContextProvider")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowLoginContextProvider passwordlessCasWebflowLoginContextProvider() {
            return new PasswordlessCasWebflowLoginContextProvider();
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordlessComponentSerializationPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ComponentSerializationPlanConfigurer passwordlessComponentSerializationPlanConfigurer() {
            return plan -> plan.registerSerializableClass(PasswordlessUserAccount.class);
        }

    }

    @Configuration(value = "PasswordlessDelegatedAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
    @ConditionalOnClass(DelegatedAuthenticationWebflowConfiguration.class)
    static class PasswordlessDelegatedAuthenticationConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "passwordlessDelegatedClientAuthenticationWebflowStateContributor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
        public DelegatedClientAuthenticationWebflowStateContributor passwordlessDelegatedClientAuthenticationWebflowStateContributor() {
            return new PasswordlessDelegatedClientAuthenticationWebflowStateContributor();
        }

        @ConditionalOnMissingBean(name = "passwordlessDelegatedClientIdentityProviderAuthorizer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DelegatedClientIdentityProviderAuthorizer passwordlessDelegatedClientIdentityProviderAuthorizer(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return new PasswordlessDelegatedClientIdentityProviderAuthorizer(servicesManager,
                registeredServiceAccessStrategyEnforcer, tenantExtractor);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action determineDelegatedAuthenticationAction(
            @Qualifier("passwordlessPrincipalFactory")
            final PrincipalFactory passwordlessPrincipalFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
            final ObjectProvider<DelegatedClientIdentityProviderConfigurationProducer> pp,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> {
                    val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
                    if (pp.getIfAvailable() != null && CasRuntimeHintsRegistrar.notInNativeImage() && scriptFactory.isPresent()) {
                        val selectorScriptResource = casProperties.getAuthn().getPasswordless().getCore()
                            .getDelegatedAuthenticationSelectorScript().getLocation();
                        return new PasswordlessDetermineDelegatedAuthenticationAction(casProperties,
                            multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport,
                            pp.getObject(), scriptFactory.get().fromResource(selectorScriptResource));
                    }
                    return new StaticEventExecutionAction(CasWebflowConstants.TRANSITION_ID_SUCCESS);
                })
                .withId(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)
                .build()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public DelegatedClientWebflowCustomizer passwordlessMultifactorWebflowCustomizer() {
            return new DelegatedClientWebflowCustomizer() {
                @Override
                public List<String> getWebflowAttributeMappings() {
                    return PasswordlessWebflowUtils.WEBFLOW_ATTRIBUTE_MAPPINGS;
                }
            };
        }
    }

    @Configuration(value = "PasswordlessCaptchaConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn, module = "captcha")
    static class PasswordlessCaptchaConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.passwordless.google-recaptcha.enabled").isTrue();

        @ConditionalOnMissingBean(name = "passwordlessCaptchaWebflowConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CasWebflowConfigurer passwordlessCaptchaWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val configurer = new PasswordlessAuthenticationCaptchaWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, applicationContext, casProperties);
                    configurer.setOrder(casProperties.getAuthn().getPasswordless().getWebflow().getOrder() + 2);
                    return configurer;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "passwordlessCaptchaWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer passwordlessCaptchaWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("passwordlessCaptchaWebflowConfigurer")
            final CasWebflowConfigurer cfg) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(cfg))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "passwordlessCaptchaActivationStrategy")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CaptchaActivationStrategy passwordlessCaptchaActivationStrategy(
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
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORDLESS_INIT_CAPTCHA)
        public Action passwordlessInitializeCaptchaAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("passwordlessCaptchaActivationStrategy")
            final CaptchaActivationStrategy passwordlessCaptchaActivationStrategy,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val recaptcha = casProperties.getAuthn().getPasswordless().getGoogleRecaptcha();
                    return new InitializeCaptchaAction(passwordlessCaptchaActivationStrategy,
                        requestContext -> PasswordlessWebflowUtils.putPasswordlessCaptchaEnabled(requestContext, recaptcha),
                        recaptcha);
                })
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "passwordlessCaptchaValidator")
        public CaptchaValidator passwordlessCaptchaValidator(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(CaptchaValidator.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> CaptchaValidator.getInstance(casProperties.getAuthn().getPasswordless().getGoogleRecaptcha()))
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORDLESS_VALIDATE_CAPTCHA)
        public Action passwordlessValidateCaptchaAction(
            final CasConfigurationProperties casProperties,
            @Qualifier("passwordlessCaptchaActivationStrategy")
            final CaptchaActivationStrategy captchaActivationStrategy,
            @Qualifier("passwordlessCaptchaValidator")
            final CaptchaValidator passwordlessCaptchaValidator,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> new ValidateCaptchaAction(passwordlessCaptchaValidator, captchaActivationStrategy))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA)
                .build()
                .get();
        }
    }
}
