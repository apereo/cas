package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.*;

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
