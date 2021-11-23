package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnCredential;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link WebAuthnMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class WebAuthnMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {
    /**
     * Webflow event id.
     */
    public static final String MFA_WEB_AUTHN_EVENT_ID = "mfa-webauthn";

    private static final String TRANSITION_ID_VALIDATE_WEBAUTHN = "validateWebAuthn";

    private final CsrfTokenRepository csrfTokenRepository;

    public WebAuthnMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                final FlowDefinitionRegistry flowDefinitionRegistry,
                                                final ConfigurableApplicationContext applicationContext,
                                                final CasConfigurationProperties casProperties,
                                                final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers,
                                                final CsrfTokenRepository csrfTokenRepository) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext,
            casProperties, Optional.of(flowDefinitionRegistry), mfaFlowCustomizers);
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void doInitialize() {
        val addCsrfTokenAction = new ConsumerExecutionAction(context -> {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            request.setAttribute(HttpServletResponse.class.getName(), response);

            var csrfToken = csrfTokenRepository.loadToken(request);
            if (csrfToken == null) {
                csrfToken = csrfTokenRepository.generateToken(request);
                csrfTokenRepository.saveToken(csrfToken, request, response);
            }
            context.getFlowScope().put(csrfToken.getParameterName(), csrfToken);
        });

        multifactorAuthenticationFlowDefinitionRegistries.forEach(registry -> {
            val flow = getFlow(registry, MFA_WEB_AUTHN_EVENT_ID);
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, WebAuthnCredential.class);

            flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
            createEndState(flow, CasWebflowConstants.STATE_ID_SUCCESS);

            val initLoginFormState = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION));
            createTransitionForState(initLoginFormState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            setStartState(flow, initLoginFormState);

            val acctRegCheckState = createActionState(flow, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION,
                createEvaluateAction("webAuthnCheckAccountRegistrationAction"));
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_REGISTER, "viewRegistrationWebAuthn");
            createTransitionForState(acctRegCheckState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);

            val setPrincipalAction = createSetAction("viewScope.principal", "conversationScope.authentication.principal");

            val viewRegState = createViewState(flow, "viewRegistrationWebAuthn", "webauthn/casWebAuthnRegistrationView");
            viewRegState.getEntryActionList().addAll(
                createEvaluateAction(CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT),
                addCsrfTokenAction,
                createEvaluateAction("webAuthnStartRegistrationAction"),
                setPrincipalAction);
            createTransitionForState(viewRegState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION);

            val saveState = createActionState(flow, CasWebflowConstants.STATE_ID_SAVE_REGISTRATION, "webAuthnSaveAccountRegistrationAction");
            createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CHECK_ACCOUNT_REGISTRATION);
            createTransitionForState(saveState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);

            val loginProperties = CollectionUtils.wrapList("token");
            val loginBinder = createStateBinderConfiguration(loginProperties);
            val viewLoginFormState = createViewState(flow,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "webauthn/casWebAuthnLoginView", loginBinder);
            createStateModelBinding(viewLoginFormState, CasWebflowConstants.VAR_ID_CREDENTIAL, WebAuthnCredential.class);
            viewLoginFormState.getEntryActionList().addAll(addCsrfTokenAction,
                createEvaluateAction("webAuthnStartAuthenticationAction"), setPrincipalAction);
            createTransitionForState(viewLoginFormState, TRANSITION_ID_VALIDATE_WEBAUTHN,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT, Map.of("bind", Boolean.TRUE, "validate", Boolean.TRUE));

            val realSubmitState = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
                createEvaluateAction("webAuthnAuthenticationWebflowAction"));
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS);
            createTransitionForState(realSubmitState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.VIEW_ID_ERROR);
        });

        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_WEB_AUTHN_EVENT_ID, webAuthn.getId());

        val flow = getLoginFlow();
        if (flow != null && webAuthn.getCore().isAllowPrimaryAuthentication()) {
            val appId = org.apache.commons.lang3.StringUtils.defaultString(webAuthn.getCore().getApplicationId(), casProperties.getServer().getName());
            val setAppIdAction = createSetAction("flowScope." + WebAuthnStartRegistrationAction.FLOW_SCOPE_WEB_AUTHN_APPLICATION_ID,
                StringUtils.quote(appId));
            flow.getStartActionList().add(setAppIdAction);

            val setPrimaryAuthAction = createSetAction("flowScope.webAuthnPrimaryAuthenticationEnabled", "true");
            flow.getStartActionList().add(setPrimaryAuthAction);

            flow.getStartActionList().add(addCsrfTokenAction);

            val viewLoginFormState = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            createTransitionForState(viewLoginFormState, TRANSITION_ID_VALIDATE_WEBAUTHN, "validateWebAuthnToken");

            val validateAction = createActionState(flow, "validateWebAuthnToken", "webAuthnValidateSessionCredentialTokenAction");
            validateAction.getEntryActionList()
                .add(createSetAction("flowScope.".concat(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID), StringUtils.quote(webAuthn.getId())));

            createTransitionForState(validateAction,
                CasWebflowConstants.TRANSITION_ID_FINALIZE, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        }
    }
}
