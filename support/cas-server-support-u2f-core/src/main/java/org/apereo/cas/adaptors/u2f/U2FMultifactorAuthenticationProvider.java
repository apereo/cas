package org.apereo.cas.adaptors.u2f;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.U2FMultifactorProperties;

/**
 * This is {@link U2FMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 157455070794156717L;

    /**
     * Required for serialization and reflection.
     */
    public U2FMultifactorAuthenticationProvider() {
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), U2FMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    
    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return "FIDO U2F";
    }
}
