package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.flow.AcceptPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DetermineDelegatedAuthenticationAction;
import org.apereo.cas.web.flow.DetermineMultifactorPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurer;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

/**
 * This is {@link PasswordlessAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration("passwordlessAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PasswordlessAuthenticationWebflowConfiguration {
    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    @Qualifier("passwordlessPrincipalFactory")
    private ObjectProvider<PrincipalFactory> passwordlessPrincipalFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("passwordlessUserAccountStore")
    private ObjectProvider<PasswordlessUserAccountStore> passwordlessUserAccountStore;

    @Autowired
    @Qualifier("passwordlessTokenRepository")
    private ObjectProvider<PasswordlessTokenRepository> passwordlessTokenRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private ObjectProvider<MultifactorAuthenticationTriggerSelectionStrategy> multifactorTriggerSelectionStrategy;

    @Autowired
    @Qualifier("delegatedClientIdentityProviderConfigurationFunction")
    private ObjectProvider<Function<RequestContext, Set<? extends Serializable>>> delegatedClientProviderFunction;

    @Bean
    @ConditionalOnMissingBean(name = "verifyPasswordlessAccountAuthenticationAction")
    @RefreshScope
    public Action verifyPasswordlessAccountAuthenticationAction() {
        return new VerifyPasswordlessAccountAuthenticationAction(passwordlessUserAccountStore.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "determineMultifactorPasswordlessAuthenticationAction")
    @RefreshScope
    public Action determineMultifactorPasswordlessAuthenticationAction() {
        return new DetermineMultifactorPasswordlessAuthenticationAction(
            multifactorTriggerSelectionStrategy.getObject(),
            passwordlessPrincipalFactory.getObject(),
            authenticationSystemSupport.getObject(),
            casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "determineDelegatedAuthenticationAction")
    @RefreshScope
    public Action determineDelegatedAuthenticationAction() {
        if (delegatedClientProviderFunction.getIfAvailable() != null) {
            val selectorScriptResource = casProperties.getAuthn().getPasswordless().getDelegatedAuthenticationSelectorScript().getLocation();
            return new DetermineDelegatedAuthenticationAction(casProperties, delegatedClientProviderFunction.getObject(),
                new WatchableGroovyScriptResource(selectorScriptResource));
        }
        return requestContext -> new EventFactorySupport().success(this);
    }

    @Bean
    @ConditionalOnMissingBean(name = "acceptPasswordlessAuthenticationAction")
    @RefreshScope
    public Action acceptPasswordlessAuthenticationAction() {
        return new AcceptPasswordlessAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject(),
            passwordlessTokenRepository.getObject(),
            authenticationSystemSupport.getObject(),
            passwordlessUserAccountStore.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "displayBeforePasswordlessAuthenticationAction")
    @RefreshScope
    public Action displayBeforePasswordlessAuthenticationAction() {
        return new DisplayBeforePasswordlessAuthenticationAction(passwordlessTokenRepository.getObject(),
            passwordlessUserAccountStore.getObject(), communicationsManager.getObject(),
            casProperties.getAuthn().getPasswordless());
    }

    @Bean
    public Action initializeLoginAction() {
        return new PrepareForPasswordlessAuthenticationAction(servicesManager.getObject(), casProperties);
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer passwordlessAuthenticationWebflowConfigurer() {
        return new PasswordlessAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }
    
    @ConditionalOnMissingBean(name = "passwordlessCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer passwordlessCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(passwordlessAuthenticationWebflowConfigurer());
    }
}
