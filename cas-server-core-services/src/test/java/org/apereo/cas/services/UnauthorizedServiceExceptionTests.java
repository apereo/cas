package org.apereo.cas.services;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class UnauthorizedServiceExceptionTests {

    private static final String MESSAGE = "GG";

    @Test
    public void verifyCodeConstructor() {
        final UnauthorizedServiceException e = new UnauthorizedServiceException(MESSAGE);

        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    public void verifyThrowableConstructorWithCode() {
        final RuntimeException r = new RuntimeException();
        final UnauthorizedServiceException e = new UnauthorizedServiceException(MESSAGE, r);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}
