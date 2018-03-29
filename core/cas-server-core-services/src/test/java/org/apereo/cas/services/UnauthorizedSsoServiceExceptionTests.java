package org.apereo.cas.services;

import static org.junit.Assert.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
public class UnauthorizedSsoServiceExceptionTests {

    private static final String CODE = "service.not.authorized.sso";
    private static final String MESSAGE = "GG";

    @Test
    public void verifyGetCode() {
        final var e = new UnauthorizedSsoServiceException();
        assertEquals(CODE, e.getMessage());
    }

    @Test
    public void verifyCodeConstructor() {
        final var e = new UnauthorizedSsoServiceException(MESSAGE);

        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    public void verifyThrowableConstructorWithCode() {
        final var r = new RuntimeException();
        final var e = new UnauthorizedSsoServiceException(MESSAGE, r);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}
