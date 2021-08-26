package org.apereo.cas.support.inwebo;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * The Inwebo MFA provider definition.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@NoArgsConstructor
public class InweboMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 7504677927348866590L;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), InweboMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public String getFriendlyName() {
        return "Inwebo";
    }
}
