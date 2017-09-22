package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;

/**
 * This is {@link U2FMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 157455070794156717L;

    @Override
    protected boolean isAvailable() {
        return true;
    }
}
