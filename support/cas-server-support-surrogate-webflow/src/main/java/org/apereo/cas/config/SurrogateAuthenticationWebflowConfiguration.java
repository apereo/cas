package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.flow.action.LoadSurrogatesListAction;
import org.apereo.cas.web.flow.action.SurrogateAuthorizationAction;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.action.SurrogateSelectionAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
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
public class SurrogateAuthenticationWebflowConfiguration implements InitializingBean {

    @Autowired
    @Qualifier("surrogatePrincipalBuilder")
    private ObjectProvider<SurrogatePrincipalBuilder> surrogatePrincipalBuilder;

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private ObjectProvider<SurrogateAuthenticationService> surrogateAuthenticationService;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("handledAuthenticationExceptions")
    private Set<Class<? extends Throwable>> handledAuthenticationExceptions;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer surrogateWebflowConfigurer() {
        return new SurrogateWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "selectSurrogateAction")
    @Bean
    public Action selectSurrogateAction() {
        return new SurrogateSelectionAction(surrogatePrincipalBuilder.getObject());
    }

    @Bean
    public Action authenticationViaFormAction() {
        return new SurrogateInitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject(),
            casProperties.getAuthn().getSurrogate().getSeparator());
    }

    @ConditionalOnMissingBean(name = "surrogateAuthorizationCheck")
    @Bean
    public Action surrogateAuthorizationCheck() {
        return new SurrogateAuthorizationAction(registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "loadSurrogatesListAction")
    @Bean
    public Action loadSurrogatesListAction() {
        return new LoadSurrogatesListAction(surrogateAuthenticationService.getObject(),
            surrogatePrincipalBuilder.getObject());
    }

    @Override
    public void afterPropertiesSet() {
        this.handledAuthenticationExceptions.add(SurrogateAuthenticationException.class);
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer surrogateCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer());
    }
}
