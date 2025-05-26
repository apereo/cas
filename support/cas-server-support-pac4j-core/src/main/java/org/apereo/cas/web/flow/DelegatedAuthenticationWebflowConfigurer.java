package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.Optional;

/**
 * The {@link DelegatedAuthenticationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DelegatedAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private final FlowDefinitionRegistry delegatedClientRedirectFlowRegistry;

    public DelegatedAuthenticationWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final FlowDefinitionRegistry delegationRedirectFlowRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        this.delegatedClientRedirectFlowRegistry = delegationRedirectFlowRegistry;
        setOrder(casProperties.getAuthn().getPac4j().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flowDefn = getLoginFlow();
        Optional.ofNullable(flowDefn).ifPresent(flow -> {
            createClientActionState(flow);
            createStopWebflowViewState(flow);
            createDelegatedClientLogoutAction();
            createClientRedirectSubflow(flow);
            createIdentityProviderLogoutState(flow);

            val selectionType = casProperties.getAuthn().getPac4j().getCore().getDiscoverySelection().getSelectionType();
            if (selectionType.isDynamic()) {
                createDynamicDiscoveryViewState(flow);
                createDynamicDiscoveryActionState(flow);
                createRedirectToProviderViewState(flow);
            }
        });
    }

    protected void createDelegatedClientLogoutAction() {
        val logoutFlow = getLogoutFlow();
        val terminateSessionState = getState(logoutFlow, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
        terminateSessionState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT));
    }

    protected void createDelegatedClientCredentialSelectionState(final Flow flow) {
        val viewState = createViewState(flow, "viewDelegatedAuthnCredentials", "delegated-authn/casDelegatedAuthnSelectionView.html");
        val selectState = createActionState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION,
            CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION);
        createTransitionForState(selectState, CasWebflowConstants.TRANSITION_ID_SELECT, viewState.getId());
        createTransitionForState(selectState, CasWebflowConstants.TRANSITION_ID_FINALIZE, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_FINALIZE);

        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SELECT, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_FINALIZE);
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_CANCEL, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);

        val finalize = createActionState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_FINALIZE,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION_FINALIZE));
        createTransitionForState(finalize, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
    }

    protected void createIdentityProviderLogoutState(final Flow flow) {
        val logoutState = createActionState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT));
        createTransitionForState(logoutState, CasWebflowConstants.TRANSITION_ID_PROCEED, "redirectToCasLogout");
        createTransitionForState(logoutState, CasWebflowConstants.TRANSITION_ID_DONE, "logoutCompleted");

        val redirectState = createActionState(flow, "redirectToCasLogout",
            CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_FINALIZE_LOGOUT);
        createTransitionForState(redirectState, CasWebflowConstants.TRANSITION_ID_LOGOUT, "logoutSubflow");
        
        val logoutSubflow = createSubflowState(flow, "logoutSubflow", CasWebflowConfigurer.FLOW_ID_LOGOUT);
        createStateDefaultTransition(logoutSubflow, "logoutCompleted");
        
        val logoutCompleted = createEndState(flow, "logoutCompleted");
        logoutCompleted.setFinalResponseAction(ConsumerExecutionAction.OK);
    }

    protected void createClientActionState(final Flow flow) {
        val delegatedAuthentication = createActionState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION));

        val transitionSet = delegatedAuthentication.getTransitionSet();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SELECT, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION));

        val currentStartState = getStartState(flow).getId();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_GENERATE, currentStartState));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_LOGOUT, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT));

        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_RESUME, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
            CasWebflowConstants.DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_STOP_WEBFLOW));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.STATE_ID_WARN));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET));

        val viewLogin = getTransitionableState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
        val delegatedClients = createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CREATE_CLIENTS);
        viewLogin.getRenderActionList().add(delegatedClients);

        createDelegatedClientCredentialSelectionState(flow);
        setStartState(flow, delegatedAuthentication);

    }

    protected void createStopWebflowViewState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE,
            "flowScope.unauthorizedRedirectUrl != null",
            CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);

        val stopWebflowState = createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.VIEW_ID_DELEGATED_AUTHENTICATION_STOP_WEBFLOW);
        stopWebflowState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_FAILURE));

        createTransitionForState(stopWebflowState, CasWebflowConstants.TRANSITION_ID_RETRY, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY);
        val retryState = createEndState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY);
        retryState.setFinalResponseAction(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY));
    }

    private void createDynamicDiscoveryViewState(final Flow flow) {
        val attributes = createTransitionAttributes(false, false);

        val discoveryViewState = createViewState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHN_DYNAMIC_DISCOVERY_VIEW, "delegated-authn/casDynamicDiscoveryView");
        createTransitionForState(discoveryViewState, CasWebflowConstants.TRANSITION_ID_EXECUTE, CasWebflowConstants.STATE_ID_DELEGATED_AUTHN_DYNAMIC_DISCOVERY_EXECUTION);
        createTransitionForState(discoveryViewState, CasWebflowConstants.TRANSITION_ID_BACK, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);

        val casLoginViewState = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        createTransitionForState(casLoginViewState, CasWebflowConstants.TRANSITION_ID_DISCOVERY,
            CasWebflowConstants.STATE_ID_DELEGATED_AUTHN_DYNAMIC_DISCOVERY_VIEW, attributes);
    }

    private void createDynamicDiscoveryActionState(final Flow flow) {
        var discoveryActionState = createActionState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHN_DYNAMIC_DISCOVERY_EXECUTION,
            CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION);
        createTransitionForState(discoveryActionState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_DELEGATED_AUTHN_DYNAMIC_DISCOVERY_VIEW);
        createTransitionForState(discoveryActionState, CasWebflowConstants.STATE_ID_REDIRECT, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_REDIRECT_TO_AUTHN_PROVIDER);
    }

    private void createRedirectToProviderViewState(final Flow flow) {
        val factory = createExternalRedirectViewFactory(
            "requestScope." + DelegatedAuthenticationDynamicDiscoveryProviderLocator.REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL);
        createViewState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_REDIRECT_TO_AUTHN_PROVIDER, factory);
    }

    private void createClientRedirectSubflow(final Flow flow) {
        val redirectFlow = buildFlow(CasWebflowConfigurer.FLOW_ID_DELEGATION_REDIRECT);
        createEndState(redirectFlow, CasWebflowConstants.STATE_ID_SUCCESS);

        val storeWebflowAction = createActionState(redirectFlow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_STORE,
            CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_STORE_WEBFLOW_STATE);
        createTransitionForState(storeWebflowAction, CasWebflowConstants.TRANSITION_ID_REDIRECT,
            CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT);

        val redirectAction = createActionState(redirectFlow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT,
            CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT);
        createTransitionForState(redirectAction, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);

        redirectFlow.setStartState(storeWebflowAction);
        delegatedClientRedirectFlowRegistry.registerFlowDefinition(redirectFlow);
        flowDefinitionRegistry.registerFlowDefinition(redirectFlow);

        val casLoginViewState = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        createTransitionForState(casLoginViewState, CasWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_REDIRECT,
            CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_SUBFLOW);

        val subflowState = createSubflowState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_SUBFLOW,
            CasWebflowConfigurer.FLOW_ID_DELEGATION_REDIRECT);
        createTransitionForState(subflowState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_END_WEBFLOW);

        val customizers = applicationContext.getBeansOfType(DelegatedClientWebflowCustomizer.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .toList();

        val attrMapping = createFlowMapping("flowScope." + CasWebflowConstants.ATTRIBUTE_SERVICE, CasWebflowConstants.ATTRIBUTE_SERVICE);
        val attrMappings = CollectionUtils.wrapList(attrMapping);
        customizers.forEach(c -> c.getWebflowAttributeMappings()
            .forEach(key -> attrMappings.add(createFlowMapping("flowScope." + key, key))));
        val attributeMapper = createFlowInputMapper(attrMappings);
        val subflowMapper = createSubflowAttributeMapper(attributeMapper, null);
        subflowState.setAttributeMapper(subflowMapper);

        val mapping = createFlowMapping(CasWebflowConstants.ATTRIBUTE_SERVICE,
            "flowScope." + CasWebflowConstants.ATTRIBUTE_SERVICE, false, Service.class);
        val flowMappings = CollectionUtils.wrapList(mapping);
        customizers.forEach(c -> c.getWebflowAttributeMappings()
            .forEach(key -> flowMappings.add(createFlowMapping(key, "flowScope." + key))));
        createFlowInputMapper(flowMappings, redirectFlow);
    }
}
