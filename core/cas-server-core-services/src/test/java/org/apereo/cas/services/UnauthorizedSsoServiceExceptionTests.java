package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class UnauthorizedSsoServiceExceptionTests {

    private static final String CODE = "service.not.authorized.sso";
    private static final String MESSAGE = "GG";

    @Test
    public void verifyGetCode() {
        val e = new UnauthorizedSsoServiceException();
        assertEquals(CODE, e.getMessage());
    }

    @Test
    public void verifyCodeConstructor() {
        val e = new UnauthorizedSsoServiceException(MESSAGE);

        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    public void verifyThrowableConstructorWithCode() {
        val r = new RuntimeException();
        val e = new UnauthorizedSsoServiceException(MESSAGE, r);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}
