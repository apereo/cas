package org.apereo.cas.gauth.web.flow;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorPrepareLoginAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        WebUtils.putGoogleAuthenticatorMultipleDeviceRegistrationEnabled(requestContext,
            casProperties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled());
        return null;
    }
}
