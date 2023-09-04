package org.apereo.cas.adaptors.authy.web.flow;

import com.okta.sdk.resource.model.UserFactor;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaPhoneNumber;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_ERROR_ID;
import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_SUCCESS_ID;

@Component("phoneEnrollmentAction")
public class PhoneEnrollmentAction implements Action {

    private final OktaService oktaService;
    private static final String MESSAGE_CODE_ERROR = "Le numéro de téléphone n'est pas correct, veuillez renseigner un numéro de téléphone valide";

    @Autowired
    public PhoneEnrollmentAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }

    @Override
    public Event execute(RequestContext context) {
        System.out.println("***************** PhoneEnrollmentAction *******************");

        // Get variables
        String userId = (String) context.getFlowScope().get("userId");
        String phoneNumber = ((OktaPhoneNumber) context.getFlowScope().get("number")).getPhoneNumber();
        System.out.println("phoneNumber: " + phoneNumber);

        // Check if the phone number is correct and add indicator
        if (phoneNumber.startsWith("0")) {
            if (phoneNumber.length() == 10) {
                phoneNumber = "+33" + phoneNumber.substring(1);
            } else {
                addErrorMessage(context);
                return new Event(this, EVENT_ERROR_ID);
            }
        } else if (phoneNumber.startsWith("+33")) {
            if (phoneNumber.length() != 12) {
                addErrorMessage(context);
                return new Event(this, EVENT_ERROR_ID);
            }
        } else if (phoneNumber.length() == 9) {
            phoneNumber = "+33" + phoneNumber;
        } else {
            addErrorMessage(context);
            return new Event(this, EVENT_ERROR_ID);
        }

        UserFactor factor = oktaService.enrollSmsFactor(userId, phoneNumber);
        String factorId = factor.getId();
        context.getFlowScope().put("factorId", factorId);
        return new Event(this, EVENT_SUCCESS_ID);
    }

    protected void addErrorMessage(final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, MESSAGE_CODE_ERROR, "Le numéro de téléphone n'est pas correct, veuillez renseigner un numéro de téléphone valide");
    }
}
