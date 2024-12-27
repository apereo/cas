package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("Simple")
class UnauthorizedSsoServiceExceptionTests {
    private static final String MESSAGE = UUID.randomUUID().toString();

    @Test
    void verifyGetCode() {
        val e = new UnauthorizedSsoServiceException();
        assertEquals(UnauthorizedSsoServiceException.CODE, e.getMessage());
    }

    @Test
    void verifyCodeConstructor() {
        val e = new UnauthorizedSsoServiceException(MESSAGE);
        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    void verifyThrowableConstructorWithCode() {
        val r = new RuntimeException();
        val e = new UnauthorizedSsoServiceException(MESSAGE, r);
        assertEquals(MESSAGE, e.getMessage());
        assertEquals(UnauthorizedSsoServiceException.CODE, e.getCode());
        assertEquals(r, e.getCause());
    }
}
