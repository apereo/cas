package org.apereo.cas.adaptors.authy.web.flow;

import com.okta.sdk.resource.model.FactorProvider;
import com.okta.sdk.resource.model.FactorType;
import com.okta.sdk.resource.model.UserFactor;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaFactorStatus;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaMfaSelect;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

import static org.apereo.cas.adaptors.authy.core.Constants.EVENT_ERROR_ID;

@Component("selectMfaAction")
public class SelectMfaAction implements Action {

    private final OktaService oktaService;

    @Autowired
    public SelectMfaAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }

    @Override
    public Event execute(RequestContext context) {
        System.out.println("****************** SelectMfaAction ********************");
        String userSelection = ((OktaMfaSelect) context.getFlowScope().get("identificationType")).getIdentificationMethod();
        String userId = (String) context.getFlowScope().get("userId");
        List<UserFactor> factors = oktaService.listUserFactors(userId);

        FactorType factorType;
        FactorProvider factorProvider;
        switch (userSelection) {
            case "sms":
                factorType = FactorType.SMS;
                factorProvider = FactorProvider.OKTA;
                break;
            case "email":
                factorType = FactorType.EMAIL;
                factorProvider = FactorProvider.OKTA;
                break;
            case "oktaVerifyPush":
                factorType = FactorType.PUSH;
                factorProvider = FactorProvider.OKTA;
                break;
            case "oktaVerifyOtp":
                factorType = FactorType.TOKEN_SOFTWARE_TOTP;
                factorProvider = FactorProvider.OKTA;
                break;
            case "googleAuth":
                factorType = FactorType.TOKEN_SOFTWARE_TOTP;
                factorProvider = FactorProvider.GOOGLE;
                break;
            default:
                return new Event(this, EVENT_ERROR_ID);
        }
        context.getFlowScope().put("factorType", factorType.toString());
        context.getFlowScope().put("factorProvider", factorProvider.toString());

        OktaFactorStatus factorStatus = oktaService.getFactorStatus(userId, factors, factorType, factorProvider);
        String factorId = factorStatus.getFactorId();
        context.getFlowScope().put("factorId", factorId);

        if (factorStatus.getFactorStatus().contains("challenge")) {
            if (factorType.equals(FactorType.PUSH)) {
            String transactionId = oktaService.sendPushFactorChallenge(userId, factorId);
            context.getFlowScope().put("transactionId", transactionId);
        } else {
           oktaService.sendFactorChallenge(userId, factorId);
        }
        }

        System.out.println("User selected factor type : " + userSelection + " (" + factorStatus.getFactorStatus() + ")");
        context.getFlowScope().put("status", factorStatus.getFactorStatus());

        return new Event(this, factorStatus.getFactorStatus());
    }
}
