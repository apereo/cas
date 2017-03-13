package org.apereo.cas.web.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.AcceptableUsagePolicyFormAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.DefaultAcceptableUsagePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasSupportActionsAcceptableUsagePolicyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsAcceptableUsagePolicyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportActionsAcceptableUsagePolicyConfiguration {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Bean
    public Action acceptableUsagePolicyFormAction(@Qualifier("acceptableUsagePolicyRepository")
                                                  final AcceptableUsagePolicyRepository repository) {
        return new AcceptableUsagePolicyFormAction(repository);
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer() {
        return new AcceptableUsagePolicyWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyRepository")
    @Bean
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        return new DefaultAcceptableUsagePolicyRepository(ticketRegistrySupport);
    }
}
