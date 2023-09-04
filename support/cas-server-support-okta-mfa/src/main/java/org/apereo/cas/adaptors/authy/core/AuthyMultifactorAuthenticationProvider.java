package org.apereo.cas.adaptors.authy.core;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.AuthyMultifactorAuthenticationProperties;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * The authentication provider for google authenticator.
 *
 * @author Jérémie POISSON
 * 
 */
@NoArgsConstructor
public class AuthyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), AuthyMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public String getFriendlyName() {
        return "Authy";
    }
}
