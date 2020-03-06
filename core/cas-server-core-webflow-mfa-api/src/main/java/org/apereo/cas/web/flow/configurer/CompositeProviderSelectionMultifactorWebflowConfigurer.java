package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CompositeProviderSelectionMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class CompositeProviderSelectionMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_COMPOSITE_EVENT_ID = "mfa-composite";

    static final String ACTION_ID_PREPARE_MULTIFACTOR_PROVIDER_SELECTION = "prepareMultifactorProviderSelectionAction";

    static final String ACTION_ID_MULTIFACTOR_PROVIDER_SELECTED = "multifactorProviderSelectedAction";

    public CompositeProviderSelectionMultifactorWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val realSubmit = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
            createTransitionForState(realSubmit, MFA_COMPOSITE_EVENT_ID, MFA_COMPOSITE_EVENT_ID);

            val viewState = createViewState(flow, MFA_COMPOSITE_EVENT_ID, "casCompositeMfaProviderSelectionView");
            viewState.getEntryActionList().add(createEvaluateAction(ACTION_ID_PREPARE_MULTIFACTOR_PROVIDER_SELECTION));

            createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.ACTION_ID_MFA_PROVIDER_SELECTED);
            val selectedState = createActionState(flow, CasWebflowConstants.ACTION_ID_MFA_PROVIDER_SELECTED,
                createEvaluateAction(ACTION_ID_MULTIFACTOR_PROVIDER_SELECTED));

            val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values();
            providers.forEach(p -> createTransitionForState(selectedState, p.getId(), p.getId()));
        }
    }
}
