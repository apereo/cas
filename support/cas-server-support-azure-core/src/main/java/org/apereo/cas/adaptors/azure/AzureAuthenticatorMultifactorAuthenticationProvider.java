package org.apereo.cas.adaptors.azure;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.AzureMultifactorProperties;

/**
 * The authentication provider for azure authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AzureAuthenticatorMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 4789727148634156909L;

    /**
     * Required for serialization and reflection.
     */
    public AzureAuthenticatorMultifactorAuthenticationProvider() {
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), AzureMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    
    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return "Microsoft Azure";
    }
}
