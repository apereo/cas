package org.apereo.cas.adaptors.authy.web.flow;

import com.okta.sdk.resource.model.FactorProvider;
import com.okta.sdk.resource.model.FactorType;
import com.okta.sdk.resource.model.UserFactor;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaCode;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Locale;

import static org.apereo.cas.adaptors.authy.core.Constants.*;

@Component("VerifyFactorAction")
public class VerifyFactorAction implements Action {

    private final OktaService oktaService;

    @Autowired
    public VerifyFactorAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }

    @Override
    public Event execute(RequestContext context) {
        System.out.println("***************** VerifyFactorAction *********************");

        // Get variable
        String passCode = ((OktaCode) context.getFlowScope().get("code")).getSecretCode();
        String factorId = (String) context.getFlowScope().get("factorId");
        String factorType = (String) context.getFlowScope().get("factorType");
        String factorProvider = (String) context.getFlowScope().get("factorProvider");
        String userId = (String) context.getFlowScope().get("userId");
        String status = (String) context.getFlowScope().get("status");

        // Check if it is a registration or a challenge
        boolean response = false;
        if (status.contains("enroll")) {
            response = oktaService.activateFactor(userId, factorId, passCode, factorType.equals(FactorType.TOKEN_SOFTWARE_TOTP.toString()) && factorProvider.equals(FactorProvider.OKTA.toString()));
        } else if (factorType.toUpperCase(Locale.ROOT).equals(FactorType.PUSH)) {
            List<UserFactor> factors = oktaService.listUserFactors(userId);
            for (UserFactor f : factors) {
                if (f.getProvider().equals(FactorProvider.OKTA) && f.getFactorType().equals(FactorType.TOKEN_SOFTWARE_TOTP)) {
                    response = oktaService.verifyFactorChallenge(userId, f.getId(), passCode);
                    break;
                }
            }
        }
        else {
            response = oktaService.verifyFactorChallenge(userId, factorId, passCode);
        }

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