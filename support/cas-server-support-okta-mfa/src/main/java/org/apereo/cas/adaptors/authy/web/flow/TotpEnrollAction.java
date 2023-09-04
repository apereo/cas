package org.apereo.cas.adaptors.authy.web.flow;

import com.okta.sdk.resource.model.FactorProvider;
import com.okta.sdk.resource.model.FactorStatus;
import com.okta.sdk.resource.model.FactorType;
import com.okta.sdk.resource.model.UserFactor;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.apereo.cas.adaptors.authy.core.Constants.*;

@Component("totpEnrollViewAction")
public class TotpEnrollAction implements Action {

    private final OktaService oktaService;

    @Autowired
    public TotpEnrollAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }
    @Override
    public Event execute(RequestContext context) {
        System.out.println("******************* TotpEnrollAction *******************");
        // Get variables

        try {
            String userId = (String) context.getFlowScope().get("userId");
            String factorId = (String) context.getFlowScope().get("factorId");
            FactorType factorType = FactorType.valueOf(((String) context.getFlowScope().get("factorType")).toUpperCase());
            FactorProvider factorProvider = FactorProvider.valueOf(((String) context.getFlowScope().get("factorProvider")).toUpperCase());

            // Call the method to enroll the user and generate the QR code
            UserFactor factor;
            if (factorType.equals(FactorType.PUSH)) {
                factor = oktaService.getFactor(userId, factorId);
                if (factor.getStatus().equals(FactorStatus.ACTIVE)){
                    return new Event(this, "enrollPushSuccess");
                } else {
                    addErrorMessage(context);
                    return new Event(this, EVENT_ERROR_ID);
                }
            }
            return new Event(this, EVENT_SUCCESS_ID);
        } catch (Exception e) {
            System.out.println("Error in TotpEnrollViewAction: " + e.getMessage());
            addErrorMessage(context);
            return new Event(this, EVENT_ERROR_ID);
        }
    }

    protected void addErrorMessage(final RequestContext requestContext) {
        WebUtils.addErrorMessageToContext(requestContext, MESSAGE_TOTP_ERROR, MESSAGE_TOTP_ERROR);
    }
}
