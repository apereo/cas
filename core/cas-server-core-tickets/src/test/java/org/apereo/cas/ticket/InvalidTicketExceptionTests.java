package org.apereo.cas.ticket;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class InvalidTicketExceptionTests {

    @Test
    public void verifyCodeNoThrowable() {
        val t = new InvalidTicketException("InvalidTicketId");
        assertEquals("INVALID_TICKET", t.getCode());
    }
}
