package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link StatelessTicketRegistryWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class StatelessTicketRegistryWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private final ObjectProvider<FlowDefinitionRegistry> accountProfileFlowRegistry;

    public StatelessTicketRegistryWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                    final ObjectProvider<FlowDefinitionRegistry> accountProfileFlowRegistry,
                                                    final ConfigurableApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
        this.accountProfileFlowRegistry = accountProfileFlowRegistry;
    }

    @Override
    protected void doInitialize() {
        val loginFlow = getLoginFlow();
        prependActionsToActionStateExecutionList(loginFlow,
            CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK,
            CasWebflowConstants.ACTION_ID_READ_SESSION_STORAGE);

        accountProfileFlowRegistry.ifAvailable(registry -> {
            val flow = getFlow(registry, CasWebflowConfigurer.FLOW_ID_ACCOUNT);
            prependActionsToActionStateExecutionList(flow,
                CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
                CasWebflowConstants.ACTION_ID_READ_SESSION_STORAGE);
        });
    }
}
