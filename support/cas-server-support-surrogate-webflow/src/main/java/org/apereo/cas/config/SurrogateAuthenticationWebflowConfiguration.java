package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.flow.action.LoadSurrogatesListAction;
import org.apereo.cas.web.flow.action.SurrogateAuthorizationAction;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.action.SurrogateSelectionAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Set;

/**
 * This is {@link SurrogateAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @author John Gasper
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Configuration("surrogateAuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateAuthenticationWebflowConfiguration implements CasWebflowExecutionPlanConfigurer, InitializingBean {

    @Autowired
    @Qualifier("surrogatePrincipalBuilder")
    private SurrogatePrincipalBuilder surrogatePrincipalBuilder;

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService surrogateAuthenticationService;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("handledAuthenticationExceptions")
    private Set<Class<? extends Throwable>> handledAuthenticationExceptions;

    @Autowired
    private ApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer surrogateWebflowConfigurer() {
        return new SurrogateWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "selectSurrogateAction")
    @Bean
    public Action selectSurrogateAction() {
        return new SurrogateSelectionAction(surrogatePrincipalBuilder);
    }

    @Bean
    public Action authenticationViaFormAction() {
        return new SurrogateInitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver,
            adaptiveAuthenticationPolicy,
            casProperties.getAuthn().getSurrogate().getSeparator());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationCheck")
    @Bean
    public Action surrogateAuthorizationCheck() {
        return new SurrogateAuthorizationAction(servicesManager, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "loadSurrogatesListAction")
    @Bean
    public Action loadSurrogatesListAction() {
        return new LoadSurrogatesListAction(surrogateAuthenticationService, surrogatePrincipalBuilder);
    }

    @Override
    public void afterPropertiesSet() {
        this.handledAuthenticationExceptions.add(SurrogateAuthenticationException.class);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(surrogateWebflowConfigurer());
    }
}
