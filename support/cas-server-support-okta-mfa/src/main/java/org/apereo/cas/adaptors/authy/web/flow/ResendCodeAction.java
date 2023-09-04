package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.core.okta.models.OktaEmailAddress;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaPhoneNumber;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_ERROR_ID;
import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_SUCCESS_ID;

@Component("resendCodeAction")
public class ResendCodeAction implements Action {

    private OktaService oktaService;

    @Autowired
    public ResendCodeAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }

    @Override
    public Event execute(RequestContext context) {
        System.out.println("***************** ResendCodeAction *********************");
        String userId = (String) context.getFlowScope().get("userId");
        String factorId = (String) context.getFlowScope().get("factorId");
        String factorType = (String) context.getFlowScope().get("factorType");
        String phoneNumber = ((OktaPhoneNumber) context.getFlowScope().get("number")).getPhoneNumber();
        String email = ((OktaEmailAddress) context.getFlowScope().get("email")).getEmailAddress();
        String status = (String) context.getFlowScope().get("status");

        if (status.contains("enroll")) {
            switch  (factorType) {
                case "sms":
                    oktaService.enrollSmsFactor(userId, phoneNumber);
                    break;
                case" email":
                    oktaService.enrollEmailFactor(userId, email);
                    break;
                default:
                    System.out.println("factorType not recognized : " + factorType);
                    return new Event(this, EVENT_ERROR_ID);
            }
        } else {
            oktaService.sendFactorChallenge(userId, factorId);
        }
        return new Event(this, EVENT_SUCCESS_ID);
    }
}
