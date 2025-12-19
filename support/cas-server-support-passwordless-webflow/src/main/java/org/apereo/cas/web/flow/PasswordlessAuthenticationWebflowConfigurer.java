package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link PasswordlessAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PasswordlessAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public PasswordlessAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                       final FlowDefinitionRegistry flowDefinitionRegistry,
                                                       final ConfigurableApplicationContext applicationContext,
                                                       final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPasswordless().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createStateInitialPasswordless(flow);
            createStateGetUserIdentifier(flow);
            createStateVerifyPasswordlessAccount(flow);
            createStateDisplaySelectionMenu(flow);
            createStateDisplayPasswordless(flow);
            createStateDetermineDelegatedAuthenticationAction(flow);
            createStateDetermineMultifactorAuthenticationAction(flow);
            createStateAcceptPasswordless(flow);
        }
    }

    private void createStateDisplaySelectionMenu(final Flow flow) {
        val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY_SELECTION_MENU, "passwordless/casPasswordlessSelectionMenuView");
        viewState.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_SELECTION_MENU));
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID);
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_PASSWORDLESS_ACCEPT_SELECTION_MENU);

        val acceptState = createActionState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_ACCEPT_SELECTION_MENU,
            CasWebflowConstants.ACTION_ID_PASSWORDLESS_ACCEPT_SELECTION_MENU);
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_PROMPT, getPromptTargetStateId(flow));
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_DISPLAY, CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY);
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_MFA, CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_MFA);
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_REDIRECT, CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN);
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID);
    }

    protected void createStateInitialPasswordless(final Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
        prependActionsToActionStateExecutionList(flow, state, createEvaluateAction(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_LOGIN));
        val targetState = state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID, CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID, true);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_SKIP, targetState);
    }

    protected void createStateAcceptPasswordless(final Flow flow) {
        val acceptAction = createEvaluateAction(CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN);
        val acceptState = createActionState(flow, CasWebflowConstants.STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION, acceptAction);
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY);

        val submission = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        val transition = (Transition) submission.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        val targetStateId = transition.getTargetStateId();
        createTransitionForState(acceptState, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetStateId);
    }

    protected void createStateDisplayPasswordless(final Flow flow) {
        val viewStateDisplay = createViewState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY, "passwordless/casPasswordlessDisplayView");
        viewStateDisplay.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DISPLAY_BEFORE_PASSWORDLESS_AUTHN));
        viewStateDisplay.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CREATE_PASSWORDLESS_AUTHN_TOKEN));
        createTransitionForState(viewStateDisplay, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION);
    }

    protected void createStateVerifyPasswordlessAccount(final Flow flow) {
        val verifyAccountState = createActionState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT,
            CasWebflowConstants.ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_ERROR,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID);

        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SELECT,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY_SELECTION_MENU);

        if (applicationContext.containsBean(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)) {
            createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN);
        } else {
            createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_MFA);
        }

        val targetStateId = getPromptTargetStateId(flow);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_PROMPT, targetStateId);
    }

    private String getPromptTargetStateId(final Flow flow) {
        val state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        val transition = state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        return transition.getTargetStateId();
    }

    protected void createStateDetermineDelegatedAuthenticationAction(final Flow flow) {
        val verifyAccountState = createActionState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN,
            CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_ERROR,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_MFA);

        val subflowState = createSubflowState(flow, "passwordlessRedirectToDelegationFlow", CasWebflowConfigurer.FLOW_ID_DELEGATION_REDIRECT);
        createTransitionForState(subflowState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_END_WEBFLOW);
        val customizers = applicationContext.getBeansOfType(DelegatedClientWebflowCustomizer.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE).toList();
        val attrMapping1 = createFlowMapping("flowScope." + CasWebflowConstants.ATTRIBUTE_SERVICE, CasWebflowConstants.ATTRIBUTE_SERVICE);
        val attrMapping2 = createFlowMapping("flowScope." + DelegationWebflowUtils.FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY,
            DelegationWebflowUtils.FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY);
        val attrMappings = CollectionUtils.wrapList(attrMapping1, attrMapping2);
        customizers.forEach(customizer -> customizer.getWebflowAttributeMappings()
            .forEach(key -> attrMappings.add(createFlowMapping("flowScope." + key, key))));
        val attributeMapper = createFlowInputMapper(attrMappings);
        val subflowMapper = createSubflowAttributeMapper(attributeMapper, null);
        subflowState.setAttributeMapper(subflowMapper);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_PROMPT, "passwordlessRedirectToDelegationFlow");
    }

    protected void createStateDetermineMultifactorAuthenticationAction(final Flow flow) {
        val verifyAccountState = createActionState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_DETERMINE_MFA,
            CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_MULTIFACTOR_AUTHN);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_DISPLAY);
        createTransitionForState(verifyAccountState, CasWebflowConstants.TRANSITION_ID_ERROR,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID);

        val cfgs = applicationContext.getBeansOfType(CasMultifactorWebflowConfigurer.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .toList();
        cfgs.forEach(cfg -> cfg.getMultifactorAuthenticationFlowDefinitionRegistries()
            .forEach(registry -> Arrays.stream(registry.getFlowDefinitionIds())
                .forEach(id -> createTransitionForState(verifyAccountState, id, id))));
    }

    protected void createStateGetUserIdentifier(final Flow flow) {
        val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_PASSWORDLESS_GET_USERID,
            "passwordless/casPasswordlessGetUserIdView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
            CasWebflowConstants.STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT);
    }
}
