package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoApiUtils;
import org.apereo.cas.mfa.accepto.web.flow.qr.AccepttoQRCodeCredential;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccepttoMultifactorValidateUserDeviceRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccepttoMultifactorValidateUserDeviceRegistrationAction extends AbstractAction {

    private final CasConfigurationProperties casProperties;
    private final AuthenticationSystemSupport authenticationSystemSupport;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val eventAttributes = new LocalAttributeMap();
        val acceptto = casProperties.getAuthn().getMfa().getAcceptto();
        val authentication = WebUtils.getInProgressAuthentication();
        val email = AccepttoApiUtils.getUserEmailAttribute(authentication, acceptto);
        try {
            if (AccepttoApiUtils.isUserDevicePaired(authentication, acceptto)) {
                val credential = new AccepttoQRCodeCredential(email);
                WebUtils.putCredential(requestContext, credential);
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_FINALIZE);
            }
        } catch (final Exception e) {
            eventAttributes.put("error", e);
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Device linked to [{}} is not paired; authentication cannot proceed", email);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_DENY, eventAttributes);
    }
}
