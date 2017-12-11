package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AzureActiveDirectoryAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AzureActiveDirectoryAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String AZURE_ACTIVEDIRECTORY_REDIRECT = "azureAdRedirect";

    public AzureActiveDirectoryAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                               final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                               final ApplicationContext applicationContext,
                                                               final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createEndState(flow, AZURE_ACTIVEDIRECTORY_REDIRECT, "flowScope.MicrosoftLoginProviderUrl", true);
            final ActionState actionState = createActionState(flow, "azureActiveDirectoryAuthenticationCheck",
                createEvaluateAction("azureActiveDirectoryAuthenticationAction"));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, AZURE_ACTIVEDIRECTORY_REDIRECT));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.TRANSITION_ID_WARN));
            final String currentStartState = getStartState(flow).getId();
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_PROCEED, currentStartState));
            setStartState(flow, actionState);
        }
    }
}
