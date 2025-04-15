package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCheckRegistrationAction;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class GoogleAuthenticatorAccountCheckRegistrationAction extends OneTimeTokenAccountCheckRegistrationAction {
    public GoogleAuthenticatorAccountCheckRegistrationAction(final OneTimeTokenCredentialRepository repository,
                                                             final CasConfigurationProperties casProperties) {
        super(repository, casProperties);
    }

    @Override
    protected Event routeToRegistration(final RequestContext requestContext, final Principal principal) {
        if (!casProperties.getAuthn().getMfa().getGauth().getCore().isDeviceRegistrationEnabled()) {
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_STOP);
        }
        return super.routeToRegistration(requestContext, principal);
    }
}
