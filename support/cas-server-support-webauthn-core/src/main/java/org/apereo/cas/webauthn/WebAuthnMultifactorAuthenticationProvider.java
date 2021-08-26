package org.apereo.cas.webauthn;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnMultifactorAuthenticationProperties;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link WebAuthnMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class WebAuthnMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 7168444238520715197L;

    @Override
    public String getFriendlyName() {
        return "WebAuthn";
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), WebAuthnMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
    }
}
