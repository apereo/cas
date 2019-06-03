package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoApiUtils;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.security.PublicKey;

/**
 * This is {@link AccepttoMultifactorDetermineUserAccountStatusAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccepttoMultifactorDetermineUserAccountStatusAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;

    private final PublicKey registrationApiPublicKey;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val eventFactorySupport = new EventFactorySupport();
        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
        val authentication = WebUtils.getInProgressAuthentication();
        val email = AccepttoApiUtils.getUserEmailAttribute(authentication, acceptto);

        try {
            LOGGER.trace("Contacting authentication API to inquire for account status of [{}]", email);
            val results = AccepttoApiUtils.authenticate(authentication, acceptto, requestContext, this.registrationApiPublicKey);

            val isApproved = results.containsKey("status")
                && ObjectUtils.defaultIfNull(results.get("status"), StringUtils.EMPTY).toString().equalsIgnoreCase("approved");

            if (isApproved) {
                LOGGER.trace("Account status is approved for [{}]. Moving on...", email);
                val credential = new AccepttoEmailCredential(email);
                WebUtils.putCredential(requestContext, credential);
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_APPROVE);
            }

            if (results.isEmpty()) {
                LOGGER.warn("No API response could be found for [{}]. Denying access...", email);
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
            }
            val success = BooleanUtils.toBoolean(results.get("success").toString());
            if (!success) {
                LOGGER.warn("API response did not return successfully for [{}]. Denying access...", email);
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
            }

            if (results.containsKey("invite_token")) {
                val originalToken = results.get("invite_token").toString();
                LOGGER.trace("Located invitation token as [{}] for [{}].", originalToken, email);

                val invitationToken = AccepttoApiUtils.decodeInvitationToken(originalToken);
                LOGGER.trace("Decoded invitation token as [{}] for [{}].", invitationToken, email);

                AccepttoWebflowUtils.setApplicationId(requestContext, acceptto.getApplicationId());
                AccepttoWebflowUtils.setInvitationToken(requestContext, invitationToken);

                if (results.containsKey("eguardian_user_id")) {
                    val eguardianUserId = CollectionUtils.firstElement(results.get("eguardian_user_id")).get();
                    AccepttoWebflowUtils.setEGuardianUserId(requestContext, eguardianUserId.toString());
                }

                val qrHash = AccepttoApiUtils.generateQRCodeHash(authentication, acceptto, invitationToken);
                LOGGER.trace("Generated QR hash [{}] for [{}] to register/pair device.", qrHash, email);
                AccepttoWebflowUtils.setInvitationTokenQRCode(requestContext, qrHash);

                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
        }
        LOGGER.trace("Account status is verified for [{}]. Proceeding to MFA flow...", email);
        return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }
}
