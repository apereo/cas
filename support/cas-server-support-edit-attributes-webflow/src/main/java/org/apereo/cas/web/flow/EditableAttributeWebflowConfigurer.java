package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link EditableAttributeWebflowConfigurer} is responsible for adjusting
 * the CAS webflow context for editable attribute integration.
 *
 * @author Marcus Watkins
 * @since 5.2
 */
public class EditableAttributeWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String EDITABLE_ATTRIBUTES_VIEW = "editableAttributesView";
    private static final String EDITABLE_ATTRIBUTES_SAVED_ACTION = "editableAttributesSavedAction";
    private static final String STATE_ID_ATTRIBUTES_CHECK = "editableAttributesCheck";

    public EditableAttributeWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
            final FlowDefinitionRegistry loginFlowDefinitionRegistry, final ApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            createVerifyActionState(flow);
            createEditableAttributesView(flow);
            createSubmitActionState(flow);
            createTransitionStateToEditableAttributes(flow);
        }
    }

    private void createTransitionStateToEditableAttributes(final Flow flow) {
        final ActionState submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_ATTRIBUTES_CHECK, true);
    }

    private ActionState getRealSubmissionState(final Flow flow) {
        return (ActionState) flow.getState(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
    }

    private EvaluateAction createEditableAttributesAction(final String actionId) {
        return createEvaluateAction("editableAttributesFormAction." + actionId
                + "(flowRequestContext, flowScope.credential, messageContext)");
    }

    private void createSubmitActionState(final Flow flow) {
        final ActionState editableAttributesSavedAction = createActionState(flow, EDITABLE_ATTRIBUTES_SAVED_ACTION,
                createEditableAttributesAction("submit"));

        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS)
                .getTargetStateId();
        editableAttributesSavedAction.getTransitionSet()
                .add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, target));
        editableAttributesSavedAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR,
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }

    private void createEditableAttributesView(final Flow flow) {
        final ViewState viewState = createViewState(flow, EDITABLE_ATTRIBUTES_VIEW, "casEditableAttributesView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, EDITABLE_ATTRIBUTES_SAVED_ACTION);
    }

    private void createVerifyActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, STATE_ID_ATTRIBUTES_CHECK,
                createEditableAttributesAction("verify"));

        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS)
                .getTargetStateId();
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, target));
        actionState.getTransitionSet().add(createTransition(
                EditableAttributeFormAction.EVENT_ID_ATTRIBUTE_VALUES_NEEDED, EDITABLE_ATTRIBUTES_VIEW));
    }
}
