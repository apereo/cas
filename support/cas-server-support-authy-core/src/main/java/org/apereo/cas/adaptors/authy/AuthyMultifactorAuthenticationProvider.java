package org.apereo.cas.adaptors.authy;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.AuthyMultifactorProperties;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 4789727148634156909L;

    /**
     * Required for serialization and reflection.
     */
    public AuthyMultifactorAuthenticationProvider() {
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), AuthyMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    
    @Override
    public String getFriendlyName() {
        return "Authy";
    }
}
