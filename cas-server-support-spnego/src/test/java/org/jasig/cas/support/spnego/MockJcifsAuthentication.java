package org.jasig.cas.support.spnego;

import jcifs.spnego.Authentication;
import jcifs.spnego.AuthenticationException;

import java.security.Principal;

/**
 *
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
public class MockJcifsAuthentication extends Authentication {
    private final Principal principal;

    private final boolean valid;

    private final byte[] outToken = new byte[] {4, 5, 6};

    public MockJcifsAuthentication(final boolean valid) {
        this.principal = new MockPrincipal("test");
        this.valid = valid;

    }

    @Override
    public byte[] getNextToken() {

        return this.valid ? this.outToken : null;
    }

    @Override
    public java.security.Principal getPrincipal() {

        return this.valid ? this.principal : null;
    }

    @Override
    public void process(final byte[] arg0) throws AuthenticationException {
        if (!this.valid) {
            throw new AuthenticationException("not valid");
        }
    }

}
