package org.apereo.cas.services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class UnauthorizedProxyingExceptionTests {

    private static final String MESSAGE = "GG";

    @Test
    public void verifyGetCode() {
        final UnauthorizedProxyingException e = new UnauthorizedProxyingException();
        assertEquals(UnauthorizedProxyingException.CODE, e.getMessage());
    }

    @Test
    public void verifyCodeConstructor() {
        final UnauthorizedProxyingException e = new UnauthorizedProxyingException(MESSAGE);

        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    public void verifyThrowableConstructorWithCode() {
        final RuntimeException r = new RuntimeException();
        final UnauthorizedProxyingException e = new UnauthorizedProxyingException(MESSAGE, r);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}
