package org.apereo.cas.services;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("Simple")
class UnauthorizedProxyingExceptionTests {

    private static final String MESSAGE = "GG";

    @Test
    void verifyGetCode() {
        val e = new UnauthorizedProxyingException();
        assertEquals(UnauthorizedProxyingException.CODE, e.getMessage());
    }

    @Test
    void verifyCodeConstructor() {
        val e = new UnauthorizedProxyingException(MESSAGE);

        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    void verifyThrowableConstructorWithCode() {
        val r = new RuntimeException();
        val e = new UnauthorizedProxyingException(MESSAGE, r);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}
