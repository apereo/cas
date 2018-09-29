package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link U2FMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@NoArgsConstructor
public class U2FMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 157455070794156717L;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), U2FMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public String getFriendlyName() {
        return "FIDO U2F";
    }
}
