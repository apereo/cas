package org.apereo.cas.adaptors.gauth;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.GAuthMultifactorProperties;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 4789727148634156909L;

    /**
     * Required for serialization and reflection.
     */
    public GoogleAuthenticatorMultifactorAuthenticationProvider() {
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), GAuthMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    
    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return "Google Authenticator";
    }
}
