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
            createTransitionForState(realSubmit, CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE, CasWebflowConstants.STATE_ID_MFA_COMPOSITE);

            val initialAuthn = getState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
            createTransitionForState(initialAuthn, CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE, CasWebflowConstants.STATE_ID_MFA_COMPOSITE);

            val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_MFA_COMPOSITE, "mfa/casCompositeMfaProviderSelectionView");
            viewState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PREPARE_MULTIFACTOR_PROVIDER_SELECTION));

            createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_MFA_PROVIDER_SELECTED);
            val selectedState = createActionState(flow, CasWebflowConstants.STATE_ID_MFA_PROVIDER_SELECTED,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_MULTIFACTOR_PROVIDER_SELECTED));

            val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values();
            providers.forEach(p -> createTransitionForState(selectedState, p.getId(), p.getId()));
        }
    }
}
