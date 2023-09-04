package org.apereo.cas.adaptors.authy.web.flow;

import com.okta.sdk.resource.model.UserFactor;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaEmailAddress;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_ERROR_ID;
import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_SUCCESS_ID;

@Component("emailEnrollmentAction")
public class EmailEnrollmentAction implements Action {

    private final OktaService oktaService;
    private static final String MESSAGE_CODE_ERROR = "Email address is not valid, enter a valid email address.";
    @Autowired
    public EmailEnrollmentAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }
    @Override
    public Event execute(RequestContext context) {
        System.out.println("******************* EmailEnrollmentAction *******************");
        // Get variables
        String userId = (String) context.getFlowScope().get("userId");
        String emailAddress = ((OktaEmailAddress) context.getFlowScope().get("email")).getEmailAddress();

        // Check if the phone number is correct and add indicator
        if(!isValidEmailAddress(emailAddress)){
            addErrorMessage(context);
            return new Event(this, EVENT_ERROR_ID);
        }

        UserFactor factor;
        try {
            factor = oktaService.enrollEmailFactor(userId, emailAddress);
        } catch (Exception e) {
            addErrorMessage(context, e);
            return new Event(this, EVENT_ERROR_ID);
        }
        context.getFlowScope().put("factorId", factor.getId());
        return new Event(this, EVENT_SUCCESS_ID);
    }

    private boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    protected void addErrorMessage(final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, MESSAGE_CODE_ERROR, MESSAGE_CODE_ERROR);
    }

    protected void addErrorMessage(final RequestContext requestContext, Exception e) {
        WebUtils.addErrorMessageToContext(requestContext, MESSAGE_CODE_ERROR, e.getMessage());
    }
}
