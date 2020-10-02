package org.apereo.cas.ticket;

import org.apereo.cas.web.support.InvalidCookieException;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Tag("Tickets")
public class InvalidTicketExceptionTests {

    @Test
    public void verifyCodeNoThrowable() {
        val t = new InvalidTicketException(new IllegalArgumentException("FailsOp"), "InvalidTicket");
        assertEquals("INVALID_TICKET", t.getCode());
    }

    @Test
    public void verifyCodeWithCause() {
        val cause = new InvalidCookieException("forbidden");
        val t = new InvalidTicketException(cause, "InvalidTicketId");
        assertEquals(cause.getCode(), t.getCode());
    }
}
