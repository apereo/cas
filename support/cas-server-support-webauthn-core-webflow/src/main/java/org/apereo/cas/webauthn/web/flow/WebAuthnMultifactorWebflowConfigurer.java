package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.webauthn.WebAuthnCredential;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link WebAuthnMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class WebAuthnMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {
    /**
     * Webflow flow id.
     */
    public static final String FLOW_ID_MFA_WEBAUTHN = "mfa-webauthn";

    public WebAuthnMultifactorWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final FlowDefinitionRegistry mfaFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext,
            casProperties, Optional.of(mfaFlowDefinitionRegistry), mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, FLOW_ID_MFA_WEBAUTHN);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, WebAuthnCredential.class);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            setStartState(flow, initLoginFormState);

            val acctRegCheckState = createActionState(flow, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_WEBAUTHN_CHECK_ACCOUNT_REGISTRATION));
            acctRegCheckState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT));
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_REGISTER,
                CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_REGISTRATION);
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");

            val viewRegState = createViewState(flow, CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_REGISTRATION, "webauthn/casWebAuthnRegistrationView");
            viewRegState.getEntryActionList().addAll(
                createEvaluateAction(CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT),
                createEvaluateAction(CasWebflowConstants.ACTION_ID_WEBAUTHN_POPULATE_CSRF_TOKEN),
                createEvaluateAction(CasWebflowConstants.ACTION_ID_WEB_AUTHN_START_REGISTRATION),
                setPrincipalAction);
            createTransitionForState(viewRegState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_WEBAUTHN_SAVE_REGISTRATION);

            val saveState = createActionState(flow, CasWebflowConstants.STATE_ID_WEBAUTHN_SAVE_REGISTRATION, CasWebflowConstants.ACTION_ID_WEBAUTHN_SAVE_ACCOUNT_REGISTRATION);
            createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);

            val loginProperties = CollectionUtils.wrapList("token");
            val loginBinder = createStateBinderConfiguration(loginProperties);
            val viewLoginFormState = createViewState(flow,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "webauthn/casWebAuthnLoginView", loginBinder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, WebAuthnCredential.class);
            viewLoginFormState.getEntryActionList().addAll(createEvaluateAction(CasWebflowConstants.ACTION_ID_WEBAUTHN_POPULATE_CSRF_TOKEN),
                createEvaluateAction(CasWebflowConstants.ACTION_ID_WEBAUTHN_START_AUTHENTICATION), setPrincipalAction);
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_VALIDATE,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, createTransitionAttributes(true, true));

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_WEBAUTHN_AUTHENTICATION_WEBFLOW));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.VIEW_ID_ERROR);
        });

        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), FLOW_ID_MFA_WEBAUTHN, webAuthn.getId());

        val flow = getLoginFlow();
        if (flow != null && webAuthn.getCore().isAllowPrimaryAuthentication()) {
            val appId = org.apache.commons.lang3.StringUtils.defaultIfBlank(webAuthn.getCore().getApplicationId(), casProperties.getServer().getName());
            val setAppIdAction = createSetAction("flowScope." + WebAuthnStartRegistrationAction.FLOW_SCOPE_WEB_AUTHN_APPLICATION_ID,
                StringUtils.quote(appId));
            flow.getStartActionList().add(setAppIdAction);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_WEBAUTHN_POPULATE_CSRF_TOKEN));

            val setPrimaryAuthAction = createSetAction("flowScope.webAuthnPrimaryAuthenticationEnabled", "true");
            flow.getStartActionList().add(setPrimaryAuthAction);

            val viewLoginFormState = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            createTransitionForState(viewLoginFormState, CasWebflowConstants.TRANSITION_ID_VALIDATE,
                CasWebflowConstants.STATE_ID_WEBAUTHN_VALIDATE);

            val validateAction = createActionState(flow, CasWebflowConstants.STATE_ID_WEBAUTHN_VALIDATE,
                CasWebflowConstants.ACTION_ID_WEBAUTHN_VALIDATE_SESSION_CREDENTIAL_TOKEN);
            validateAction.getEntryActionList()
                .add(createSetAction("flowScope.".concat(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID), StringUtils.quote(webAuthn.getId())));

            createTransitionForState(validateAction,
                CasWebflowConstants.TRANSITION_ID_FINALIZE, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        }
    }
}
