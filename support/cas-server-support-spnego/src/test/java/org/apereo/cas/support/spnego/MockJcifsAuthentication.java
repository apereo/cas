package org.apereo.cas.support.spnego;

import java.security.Principal;

import jcifs.spnego.Authentication;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
@Slf4j
public class MockJcifsAuthentication extends Authentication {

    private Principal principal;
    private byte[] outToken = new byte[] {4, 5, 6};

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
        // empty
    }
}
