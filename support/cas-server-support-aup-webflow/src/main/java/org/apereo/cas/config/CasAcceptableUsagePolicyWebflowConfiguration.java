package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.AcceptableUsagePolicyFormAction;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.aup.DefaultAcceptableUsagePolicyRepository;
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

/**
 * This is {@link CasAcceptableUsagePolicyWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casAcceptableUsagePolicyWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasAcceptableUsagePolicyWebflowConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Bean
    public Action acceptableUsagePolicyFormAction(@Qualifier("acceptableUsagePolicyRepository")
                                                  final AcceptableUsagePolicyRepository repository) {
        return new AcceptableUsagePolicyFormAction(repository);
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer() {
        final CasWebflowConfigurer w = new AcceptableUsagePolicyWebflowConfigurer(flowBuilderServices, 
                loginFlowDefinitionRegistry,
                applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyRepository")
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        return new DefaultAcceptableUsagePolicyRepository(ticketRegistrySupport);
    }
}
