package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.apereo.cas.adaptors.authy.core.Constants.*;

@Component("pushValidationAction")
public class PushValidationAction implements Action {

    private final OktaService oktaService;

    @Autowired
    public PushValidationAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }

    @Override
    public Event execute(RequestContext context) {
        System.out.println("***************** VerifyFactorAction *********************");

        // Get variable
        String userId = (String) context.getFlowScope().get("userId");
        String factorId = (String) context.getFlowScope().get("factorId");
        String transactionId = (String) context.getFlowScope().get("transactionId");

        boolean response = oktaService.verifyPushFactorChallenge(userId, factorId, transactionId);
        if (response) {
            return new Event(this, EVENT_SUCCESS_ID);
        } else {
            addErrorMessage(context);
            return new Event(this, EVENT_ERROR_ID);
        }
    }

    protected void addErrorMessage(final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, MESSAGE_CODE_ERROR, MESSAGE_CODE_ERROR);
    }
}