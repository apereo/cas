package org.apereo.cas.ticket;

import static org.junit.Assert.*;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Slf4j
public class InvalidTicketExceptionTests {

    @Test
    public void verifyCodeNoThrowable() {
        val t = new InvalidTicketException("InvalidTicketId");
        assertEquals("INVALID_TICKET", t.getCode());
    }
}
