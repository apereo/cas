package org.apereo.cas.web.flow.configurer.acct;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AccountProfileContributionWebflowConfigurer}.
 * Lets a module contribute to the account profile: render actions that put data
 * into the flow scope for the profile view, and operations that the profile view
 * triggers by event id. Each operation runs in its own action state and returns
 * to the profile view on success and on error.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class AccountProfileContributionWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private final List<String> renderActionIds = new ArrayList<>();

    private final Map<String, String> operations = new LinkedHashMap<>();

    public AccountProfileContributionWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                       final FlowDefinitionRegistry flowDefinitionRegistry,
                                                       final ConfigurableApplicationContext applicationContext,
                                                       final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    /**
     * Add a render action to the profile view.
     *
     * @param actionId the action id
     * @return this configurer
     */
    public AccountProfileContributionWebflowConfigurer withRenderAction(final String actionId) {
        renderActionIds.add(actionId);
        return this;
    }

    /**
     * Add an operation that the profile view triggers with the given event.
     *
     * @param eventId  the event id submitted by the profile view
     * @param actionId the action that carries out the operation
     * @return this configurer
     */
    public AccountProfileContributionWebflowConfigurer withOperation(final String eventId, final String actionId) {
        operations.put(eventId, actionId);
        return this;
    }

    @Override
    protected void doInitialize() {
        val accountFlow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        if (accountFlow != null) {
            val accountView = getState(accountFlow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, ViewState.class);
            renderActionIds.forEach(actionId -> accountView.getRenderActionList().add(createEvaluateAction(actionId)));
            operations.forEach((eventId, actionId) -> {
                val operationState = createActionState(accountFlow, eventId + "AccountProfileOperation", actionId);
                createTransitionForState(operationState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
                createTransitionForState(operationState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
                createTransitionForState(accountView, eventId, operationState.getId());
            });
            customizeAccountFlow(accountFlow, accountView);
        }
    }

    /**
     * Customize the account flow further, after render actions and operations are added.
     *
     * @param accountFlow the account flow
     * @param accountView the account profile view
     */
    protected void customizeAccountFlow(final Flow accountFlow, final ViewState accountView) {
    }
}
