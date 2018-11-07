package org.apereo.cas.support.spnego;

import jcifs.spnego.Authentication;
import jcifs.spnego.AuthenticationException;
import lombok.AllArgsConstructor;

import java.security.Principal;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @author Sven Rieckhoff
 * @since 5.2.0
 */
@AllArgsConstructor
public class MockUnsuccessfulJcifsAuthentication extends Authentication {

    private final boolean throwExceptionOnProcess;

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
            throw new AuthenticationException("not valid");
        }
    }
}
