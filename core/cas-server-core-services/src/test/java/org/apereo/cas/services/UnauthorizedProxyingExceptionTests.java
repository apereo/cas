package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class UnauthorizedProxyingExceptionTests {

    private static final String MESSAGE = "GG";

    @Test
    public void verifyGetCode() {
        val e = new UnauthorizedProxyingException();
        assertEquals(UnauthorizedProxyingException.CODE, e.getMessage());
    }

    @Test
    public void verifyCodeConstructor() {
        val e = new UnauthorizedProxyingException(MESSAGE);

        assertEquals(MESSAGE, e.getMessage());
    }

    @Test
    public void verifyThrowableConstructorWithCode() {
        val r = new RuntimeException();
        val e = new UnauthorizedProxyingException(MESSAGE, r);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}
