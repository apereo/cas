package org.apereo.cas.qr.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link QRAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class QRAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    static final String STATE_ID_VALIDATE_QR_TOKEN = "validateQRToken";

    public QRAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            val setAction = createSetAction("flowScope.qrAuthenticationEnabled", "true");
            state.getEntryActionList().add(setAction);

            val qrSubmission = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            createTransitionForState(qrSubmission, CasWebflowConstants.TRANSITION_ID_VALIDATE, STATE_ID_VALIDATE_QR_TOKEN);

            val validateAction = createActionState(flow, STATE_ID_VALIDATE_QR_TOKEN, "qrAuthenticationValidateWebSocketChannelAction");
            createTransitionForState(validateAction, CasWebflowConstants.TRANSITION_ID_FINALIZE, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
            state.getEntryActionList().add(createEvaluateAction("qrAuthenticationGenerateCodeAction"));
        }
    }
}
