package org.apereo.cas.qr.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

@Slf4j
public class QRLoginWebflowConfigurer extends AbstractCasWebflowConfigurer {

    //static final String STATE_ID_VALIDATE_QR_TOKEN = "validateQRToken";
    //static final String STATE_ID_VALIDATE_QR = "validateQR";

    public QRLoginWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry mainFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
	LOGGER.debug("~!~ I'm in doInitialize()");
        val flow = getLoginFlow();
        if (flow != null) {
	    LOGGER.debug("~!~ flow is NOT null");
            val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            val setAction = createSetAction("flowScope.qrLoginEnabled", "true");
            state.getEntryActionList().add(setAction);

            //val qrSubmission = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            //createTransitionForState(qrSubmission, CasWebflowConstants.TRANSITION_ID_VALIDATE, STATE_ID_VALIDATE_QR);

            //val validateAction = createActionState(flow, STATE_ID_VALIDATE_QR_TOKEN, "qrAuthenticationValidateWebSocketChannelAction");
            //createTransitionForState(validateAction, CasWebflowConstants.TRANSITION_ID_FINALIZE, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
            //state.getEntryActionList().add(createEvaluateAction("qrLoginGenerateCodeAction"));
        }
	LOGGER.debug("~!~ exiting doInitialize()");
    }

}
