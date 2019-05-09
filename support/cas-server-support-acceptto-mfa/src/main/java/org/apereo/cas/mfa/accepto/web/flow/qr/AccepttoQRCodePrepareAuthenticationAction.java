package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.login.InitializeLoginAction;

import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccepttoQRCodePrepareAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class AccepttoQRCodePrepareAuthenticationAction extends InitializeLoginAction {
    public AccepttoQRCodePrepareAuthenticationAction(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        return new EventFactorySupport()
            .event(this, AccepttoQRCodeAuthenticationWebflowConfigurer.TRANSITION_ID_GENERATE_QR_CODE);
    }
}

