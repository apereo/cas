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
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.AcceptPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.DelegatedClientWebflowCustomizer;
import org.apereo.cas.web.flow.DetermineMultifactorPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PasswordlessCasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.flow.PrepareForPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.VerifyPasswordlessAccountAuthenticationAction;
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
 * This is {@link PasswordlessAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn)
@AutoConfiguration
public class PasswordlessAuthenticationWebflowConfiguration {

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
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new AcceptPasswordlessAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
                passwordlessTokenRepository, authenticationSystemSupport, passwordlessUserAccountStore,
                applicationContext))
            .withId(CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN)
            .build()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DISPLAY_BEFORE_PASSWORDLESS_AUTHN)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action displayBeforePasswordlessAuthenticationAction(
        @Qualifier(PasswordlessRequestParser.BEAN_NAME)
        final PasswordlessRequestParser passwordlessRequestParser,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CommunicationsManager.BEAN_NAME)
        final CommunicationsManager communicationsManager,
        @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
        final PasswordlessUserAccountStore passwordlessUserAccountStore,
        @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
        final PasswordlessTokenRepository passwordlessTokenRepository) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new DisplayBeforePasswordlessAuthenticationAction(
                casProperties, passwordlessTokenRepository,
                passwordlessUserAccountStore, communicationsManager, passwordlessRequestParser))
            .withId(CasWebflowConstants.ACTION_ID_DISPLAY_BEFORE_PASSWORDLESS_AUTHN)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_LOGIN)
    public Action passswordPrepareLoginAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new PrepareForPasswordlessAuthenticationAction(casProperties))
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
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new PasswordlessAuthenticationWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
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

    @Configuration(value = "PasswordlessDelegatedAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
    @ConditionalOnClass(DelegatedAuthenticationWebflowConfiguration.class)
    public static class PasswordlessDelegatedAuthenticationConfiguration {

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
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return new PasswordlessDelegatedClientIdentityProviderAuthorizer(servicesManager,
                registeredServiceAccessStrategyEnforcer);
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action determineDelegatedAuthenticationAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
            final ObjectProvider<DelegatedClientIdentityProviderConfigurationProducer> pp,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> {
                    if (pp.getIfAvailable() != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
                        val selectorScriptResource = casProperties.getAuthn().getPasswordless().getCore().getDelegatedAuthenticationSelectorScript().getLocation();
                        return new PasswordlessDetermineDelegatedAuthenticationAction(casProperties,
                            pp.getObject(), new WatchableGroovyScriptResource(selectorScriptResource));
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
}
