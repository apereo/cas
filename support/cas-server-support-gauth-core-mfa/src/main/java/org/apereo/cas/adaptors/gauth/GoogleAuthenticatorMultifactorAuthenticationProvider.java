package org.apereo.cas.adaptors.gauth;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.GAuthMultifactorProperties;
import lombok.NoArgsConstructor;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@NoArgsConstructor
public class GoogleAuthenticatorMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), GAuthMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public String getFriendlyName() {
        return "Google Authenticator";
    }
}
