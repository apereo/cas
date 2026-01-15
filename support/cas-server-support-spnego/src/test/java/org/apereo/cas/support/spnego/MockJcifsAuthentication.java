package org.apereo.cas.support.spnego;

import module java.base;
import jcifs.spnego.Authentication;
import lombok.Getter;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 4.2.0
 */
public class MockJcifsAuthentication extends Authentication {

    @Getter
    private final Principal principal;

    private final byte[] outToken = {4, 5, 6};

    public MockJcifsAuthentication() {
        this.principal = new MockPrincipal("test");
    }

    @Override
    public byte[] getNextToken() {
        return this.outToken;
    }

    @Override
    public void process(final byte[] arg0) {
    }
}
