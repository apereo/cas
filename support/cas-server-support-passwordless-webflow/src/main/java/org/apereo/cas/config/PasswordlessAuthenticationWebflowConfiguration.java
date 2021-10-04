package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.flow.AcceptPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.DetermineDelegatedAuthenticationAction;
import org.apereo.cas.web.flow.DetermineMultifactorPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PasswordlessCasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.PrepareForPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.VerifyPasswordlessAccountAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link PasswordlessAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "passwordlessAuthenticationWebflowConfiguration", proxyBeanMethods = false)
public class PasswordlessAuthenticationWebflowConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "verifyPasswordlessAccountAuthenticationAction")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action verifyPasswordlessAccountAuthenticationAction(
        @Qualifier("passwordlessUserAccountStore")
        final PasswordlessUserAccountStore passwordlessUserAccountStore) {
        return new VerifyPasswordlessAccountAuthenticationAction(passwordlessUserAccountStore);
    }

    @Bean
    @ConditionalOnMissingBean(name = "determineMultifactorPasswordlessAuthenticationAction")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action determineMultifactorPasswordlessAuthenticationAction(
        final CasConfigurationProperties casProperties,
        @Qualifier("passwordlessPrincipalFactory")
        final PrincipalFactory passwordlessPrincipalFactory,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("defaultMultifactorTriggerSelectionStrategy")
        final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy) {
        return new DetermineMultifactorPasswordlessAuthenticationAction(multifactorTriggerSelectionStrategy,
            passwordlessPrincipalFactory, authenticationSystemSupport, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "determineDelegatedAuthenticationAction")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action determineDelegatedAuthenticationAction(
        @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
        final ObjectProvider<DelegatedClientIdentityProviderConfigurationProducer> pp,
        final CasConfigurationProperties casProperties) {
        if (pp.getIfAvailable() != null) {
            val selectorScriptResource = casProperties.getAuthn().getPasswordless().getCore().getDelegatedAuthenticationSelectorScript().getLocation();
            return new DetermineDelegatedAuthenticationAction(casProperties, pp.getObject(), new WatchableGroovyScriptResource(selectorScriptResource));
        }
        return requestContext -> new EventFactorySupport().success(this);
    }

    @Bean
    @ConditionalOnMissingBean(name = "acceptPasswordlessAuthenticationAction")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action acceptPasswordlessAuthenticationAction(
        @Qualifier("passwordlessUserAccountStore")
        final PasswordlessUserAccountStore passwordlessUserAccountStore,
        @Qualifier("passwordlessTokenRepository")
        final PasswordlessTokenRepository passwordlessTokenRepository,
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        return new AcceptPasswordlessAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
            passwordlessTokenRepository, authenticationSystemSupport, passwordlessUserAccountStore);
    }

    @Bean
    @ConditionalOnMissingBean(name = "displayBeforePasswordlessAuthenticationAction")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action displayBeforePasswordlessAuthenticationAction(
        final CasConfigurationProperties casProperties,
        @Qualifier("communicationsManager")
        final CommunicationsManager communicationsManager,
        @Qualifier("passwordlessUserAccountStore")
        final PasswordlessUserAccountStore passwordlessUserAccountStore,
        @Qualifier("passwordlessTokenRepository")
        final PasswordlessTokenRepository passwordlessTokenRepository) {
        return new DisplayBeforePasswordlessAuthenticationAction(passwordlessTokenRepository, passwordlessUserAccountStore, communicationsManager,
            casProperties.getAuthn().getPasswordless());
    }

    @Bean
    @Autowired
    public Action initializeLoginAction(final CasConfigurationProperties casProperties,
                                        @Qualifier(ServicesManager.BEAN_NAME)
                                        final ServicesManager servicesManager) {
        return new PrepareForPasswordlessAuthenticationAction(servicesManager, casProperties);
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer passwordlessAuthenticationWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new PasswordlessAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "passwordlessCasWebflowExecutionPlanConfigurer")
    @Bean
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
}
