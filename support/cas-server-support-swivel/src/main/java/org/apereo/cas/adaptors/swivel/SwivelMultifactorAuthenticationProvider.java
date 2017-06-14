package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;

/**
 * This is {@link SwivelMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 498455080794156917L;

    @Override
    protected boolean isAvailable() {
        return true;
    }
}
