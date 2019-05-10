package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AccepttoQRCodeAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class AccepttoQRCodeAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Transition id to create QR code.
     */
    public static final String TRANSITION_ID_GENERATE_QR_CODE = "accepttoCreateQRCode";

    public AccepttoQRCodeAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                         final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                         final ApplicationContext applicationContext,
                                                         final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            createTransitionForState(state,
                TRANSITION_ID_GENERATE_QR_CODE, "accepttoPasswordlessQRCodeLogin");

            val viewState = createViewState(flow,
                "accepttoPasswordlessQRCodeLogin", "casAccepttoPasswordlessQRCodeLoginView");
            createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "accepttoQRCodeValidate");
            val applicationId = casProperties.getAuthn().getMfa().getAcceptto().getApplicationId();
            val setAction = createSetAction("flowScope.accepttoApplicationId", StringUtils.quote(applicationId));
            viewState.getRenderActionList().add(setAction);

            val validateAction = createActionState(flow,
                "accepttoQRCodeValidate", "mfaAccepttoQRCodeValidateWebSocketChannelAction");
            createTransitionForState(validateAction,
                CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
            createTransitionForState(validateAction,
                CasWebflowConstants.TRANSITION_ID_FINALIZE, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);

        }
    }
}
