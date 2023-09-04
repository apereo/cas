package org.apereo.cas.adaptors.authy.web.flow;

import com.okta.sdk.resource.model.FactorProvider;
import com.okta.sdk.resource.model.FactorType;
import com.okta.sdk.resource.model.UserFactor;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_ERROR_ID;
import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_SUCCESS_ID;

@Component("totpEnrollViewAction")
public class TotpEnrollViewAction implements Action {

    private final OktaService oktaService;

    @Autowired
    public TotpEnrollViewAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }
    @Override
    public Event execute(RequestContext context) {
        System.out.println("******************* TotpEnrollViewAction *******************");
        // Get variables

        try {
            String userId = (String) context.getFlowScope().get("userId");
            String factorType = (String) context.getFlowScope().get("factorType");
            String factorProvider = (String) context.getFlowScope().get("factorProvider");

            // Call the method to enroll the user and generate the QR code
            UserFactor factor;
            if (factorType.equals(FactorType.PUSH.getValue())) {
                factor = oktaService.enrollPushFactor(userId);
            } else {
                factor = oktaService.enrollTotpFactor(userId, FactorProvider.valueOf(factorProvider));
            }
            context.getFlowScope().put("factorId", factor.getId());

            Map<String, Object> embedded = factor.getEmbedded();
            Map<String, Map> activation = (Map<String, Map>) embedded.get("activation");
            Map<String, Map> links = (Map<String, Map>) activation.get("_links");
            Map<String, String> qrcode = (Map<String, String>) links.get("qrcode");
            String qrCodeUrl = qrcode.get("href");

            // Put the QR code data into the flow scope so that it can be accessed by the view
            context.getFlowScope().put("qrCodeData", qrCodeUrl);
            return new Event(this, EVENT_SUCCESS_ID);
        } catch (Exception e) {
            System.out.println("Error in TotpEnrollViewAction: " + e.getMessage());
            return new Event(this, EVENT_ERROR_ID);
        }
    }
}
