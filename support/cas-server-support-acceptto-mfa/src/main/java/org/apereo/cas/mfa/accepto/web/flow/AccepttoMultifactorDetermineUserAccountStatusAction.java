package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoApiUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val eventFactorySupport = new EventFactorySupport();
        try {
            val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
            val authentication = WebUtils.getInProgressAuthentication();

            if (AccepttoApiUtils.isUserDevicePaired(authentication, acceptto)) {
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS);
            }
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val results = AccepttoApiUtils.authenticate(authentication, acceptto, request);

            if (results.isEmpty()) {
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
            }
            val success = BooleanUtils.toBoolean(results.get("success").toString());
            if (!success) {
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_DENY);
            }

            if (results.containsKey("invite_token")) {
                val invitationToken = AccepttoApiUtils.decodeInvitationToken(results.get("invite_token").toString());
                val eguardianUserId = results.get("eguardian_user_id").toString();

                AccepttoWebflowUtils.setApplicationId(requestContext, acceptto.getApplicationId());
                AccepttoWebflowUtils.setInvitationToken(requestContext, invitationToken);
                AccepttoWebflowUtils.setEGuardianUserId(authentication, eguardianUserId);

                val qrHash = AccepttoApiUtils.generateQRCodeHash(authentication, acceptto, invitationToken);
                AccepttoWebflowUtils.setInvitationTokenQRCode(requestContext, qrHash);
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }
}
