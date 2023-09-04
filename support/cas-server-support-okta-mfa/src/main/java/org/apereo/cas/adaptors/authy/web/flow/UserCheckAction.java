package org.apereo.cas.adaptors.authy.web.flow;

import com.okta.sdk.resource.model.FactorType;
import com.okta.sdk.resource.model.User;
import com.okta.sdk.resource.model.UserFactor;
import org.apereo.cas.adaptors.authy.core.okta.OktaService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

import static org.apereo.cas.adaptors.authy.core.Constants.*;

@Component("userCheckAction")
public class UserCheckAction implements Action {

    private final OktaService oktaService;

    @Autowired
    public UserCheckAction(OktaService oktaService) {
        this.oktaService = oktaService;
    }

    @Override
    public Event execute(RequestContext context) {
        System.out.println("****************** UserCheckAction ********************");
        System.out.println("principal: " + WebUtils.getAuthentication(context).getPrincipal());

        String email = WebUtils.getAuthentication(context).getPrincipal().getId();
        User user = oktaService.findUserByEmail(email);
        if (user != null) {
            context.getFlowScope().put("userId", user.getId());
            List<UserFactor> factors = oktaService.listUserFactors(user.getId());

            UserFactor factor = oktaService.hasAnyFactorEnrolled(user.getId(), factors);
            if (factor != null) {
                // challenge
                System.out.println("Send challenge to enrolled factor : " + factor.getId());
                context.getFlowScope().put("factorId", factor.getId());
                context.getFlowScope().put("factorType", factor.getFactorType().toString());
                context.getFlowScope().put("factorProvider", factor.getProvider().toString());

                String challenge;
                if (factor.getFactorType().equals(FactorType.PUSH)) {
                    challenge = CHALLENGE_PUSH;
                    String transactionId = oktaService.sendPushFactorChallenge(user.getId(), factor.getId());
                    context.getFlowScope().put("transactionId", transactionId);
                } else {
                    challenge = CHALLENGE;
                    oktaService.sendFactorChallenge(user.getId(), factor.getId());
                }
                context.getFlowScope().put("status", challenge);
                return new Event(this, challenge);
            } else {
                System.out.println("No factor enrolled - Going to state : " + SELECT_MFA);
                context.getFlowScope().put("status", SELECT_MFA);
                return new Event(this, SELECT_MFA);
            }
        } else {
            System.out.println("User not found in Okta");
            return new Event(this, EVENT_ERROR_ID);
        }
    }
}