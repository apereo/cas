package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.JsonResourceSurrogateAuthenticationService;
import org.apereo.cas.authentication.SimpleSurrogateAuthenticationService;
import org.apereo.cas.authentication.SurrogateAuthenticationAspect;
import org.apereo.cas.authentication.SurrogateAuthenticationService;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SurrogateInitialAuthenticationAction;
import org.apereo.cas.web.flow.SurrogateSelectionAction;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link SurrogateAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("surrogateAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy
public class SurrogateAuthenticationConfiguration implements AuthenticationEventExecutionPlanConfigurer {

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

    @ConditionalOnMissingBean(name = "surrogateWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer surrogateWebflowConfigurer() {
        return new SurrogateWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, selectSurrogateAction());
    }

    @ConditionalOnMissingBean(name = "selectSurrogateAction")
    @Bean
    public Action selectSurrogateAction() {
        return new SurrogateSelectionAction(casProperties.getAuthn().getSurrogate().getSeparator());
    }

    @Bean
    public Action authenticationViaFormAction() {
        return new SurrogateInitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                casProperties.getAuthn().getSurrogate().getSeparator(),
                surrogateAuthenticationService());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "surrogateAuthenticationService")
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
        if (su.getJson().getConfig().getLocation() != null) {
            return new JsonResourceSurrogateAuthenticationService(su.getJson().getConfig().getLocation());
        }
        final Map<String, Set> accounts = new LinkedHashMap<>();
        su.getSimple().getSurrogates().forEach((k, v) -> accounts.put(k, StringUtils.commaDelimitedListToSet(v)));
        return new SimpleSurrogateAuthenticationService(accounts);
    }

    @Bean
    public SurrogateAuthenticationAspect surrogateAuthenticationAspect() {
        return new SurrogateAuthenticationAspect();
    }
}
