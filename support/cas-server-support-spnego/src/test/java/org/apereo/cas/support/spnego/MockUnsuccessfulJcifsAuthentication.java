package org.apereo.cas.support.spnego;

import java.security.Principal;

import jcifs.spnego.Authentication;
import jcifs.spnego.AuthenticationException;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @author Sven Rieckhoff
 * @since 5.2.0
 */
public class MockUnsuccessfulJcifsAuthentication extends Authentication {

    private boolean throwExceptionOnProcess;

    public MockUnsuccessfulJcifsAuthentication(final boolean throwExceptionOnProcess) {
        this.throwExceptionOnProcess = throwExceptionOnProcess;
    }

    @Override
    public byte[] getNextToken() {
        return null;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public void process(final byte[] arg0) throws AuthenticationException {
        if (this.throwExceptionOnProcess) {
            throw new AuthenticationException("not valid"); //$NON-NLS-1$
        }
    }
}
