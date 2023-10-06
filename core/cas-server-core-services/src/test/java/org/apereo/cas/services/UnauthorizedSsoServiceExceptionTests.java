package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("Simple")
class UnauthorizedSsoServiceExceptionTests {

    private static final String CODE = "service.not.authorized.sso";

    private static final String MESSAGE = "GG";

    @Test
    void verifyGetCode() throws Throwable {
        val e = new UnauthorizedSsoServiceException();
        assertEquals(CODE, e.getMessage());
    }

    @Test
    void verifyCodeConstructor() throws Throwable {
        val e = new UnauthorizedSsoServiceException(MESSAGE);

        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    void verifyThrowableConstructorWithCode() throws Throwable {
        val r = new RuntimeException();
        val e = new UnauthorizedSsoServiceException(MESSAGE, r);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}
