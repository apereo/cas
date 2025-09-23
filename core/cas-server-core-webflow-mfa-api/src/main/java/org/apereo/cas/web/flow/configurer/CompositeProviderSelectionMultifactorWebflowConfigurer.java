package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link CompositeProviderSelectionMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class CompositeProviderSelectionMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public CompositeProviderSelectionMultifactorWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        Stream.of(getLoginFlow(), getFlow(FLOW_ID_PASSWORD_RESET))
            .filter(Objects::nonNull)
            .forEach(this::configureFlow);
    }

    @Override
    public void postInitialization(final ConfigurableApplicationContext applicationContext) {
        val mfaOptional = casProperties.getAuthn().getMfa().getCore().getProviderSelection().isProviderSelectionOptional();
        if (mfaOptional) {
            val flow = getLoginFlow();
            val realSubmit = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
            val targetSuccess = realSubmit.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            val selectedState = createActionState(flow, CasWebflowConstants.STATE_ID_MFA_PROVIDER_SELECTED);
            createTransitionForState(selectedState, CasWebflowConstants.TRANSITION_ID_SKIP, targetSuccess.getTargetStateId());
        }
    }

    private void configureFlow(final Flow flow) {
        val compositeCheckAction = createActionState(flow, CasWebflowConstants.STATE_ID_MFA_COMPOSITE_SELECTION, CasWebflowConstants.ACTION_ID_MFA_COMPOSITE_SELECTION);
        createTransitionForState(compositeCheckAction, CasWebflowConstants.TRANSITION_ID_PROCEED, CasWebflowConstants.STATE_ID_MFA_COMPOSITE);
        createTransitionForState(compositeCheckAction, CasWebflowConstants.TRANSITION_ID_SELECT, CasWebflowConstants.STATE_ID_MFA_PROVIDER_SELECTED);

        val realSubmit = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        createTransitionForState(realSubmit, CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE, compositeCheckAction.getId());

        Stream.of(
                getState(flow, CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS),
                getState(flow, CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET),
                getState(flow, CasWebflowConstants.STATE_ID_WS_FEDERATION_START),
                getState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION)
            )
            .filter(Objects::nonNull)
            .forEach(state -> createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE, CasWebflowConstants.STATE_ID_MFA_COMPOSITE));

        val initialAuthn = getState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
        createTransitionForState(initialAuthn, CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE, CasWebflowConstants.STATE_ID_MFA_COMPOSITE);
        
        val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_MFA_COMPOSITE, "mfa/casCompositeMfaProviderSelectionView");
        viewState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PREPARE_MULTIFACTOR_PROVIDER_SELECTION));

        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_MFA_PROVIDER_SELECTED);
        val selectedState = createActionState(flow, CasWebflowConstants.STATE_ID_MFA_PROVIDER_SELECTED,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_MULTIFACTOR_PROVIDER_SELECTED));

        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values();
        providers.forEach(provider -> createTransitionForState(selectedState, provider.getId(), provider.getId()));
    }
}
