package org.apereo.cas.support.spnego;

import jcifs.spnego.Authentication;

import java.security.Principal;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
public class MockJcifsAuthentication extends Authentication {

    private final Principal principal;
    private final byte[] outToken = new byte[]{4, 5, 6};

    public MockJcifsAuthentication() {
        this.principal = new MockPrincipal("test");
    }

    @Override
    public byte[] getNextToken() {
        return this.outToken;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    @Override
    public void process(final byte[] arg0) {
    }
}
