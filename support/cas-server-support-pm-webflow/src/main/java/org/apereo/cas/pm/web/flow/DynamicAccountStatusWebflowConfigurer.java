package org.apereo.cas.pm.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link DynamicAccountStatusWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DynamicAccountStatusWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private static final String ACTION_GEN_SERVICE_TICKET_AFTER_ACCT_CHECK = "generateServiceTicketAfterAccountStateCheck";

    public DynamicAccountStatusWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createEvaluateActionForActionState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, "checkAccountStateAction");
            cloneAndCreateActionState(flow, ACTION_GEN_SERVICE_TICKET_AFTER_ACCT_CHECK, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        }
    }
}
