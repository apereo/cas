package org.apereo.cas.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
@Slf4j
public class DefaultUniqueTicketIdGeneratorTests {

    @Test
    public void verifyUniqueGenerationOfTicketIds() {
        final var generator = new DefaultUniqueTicketIdGenerator(10);

        assertNotSame(generator.getNewTicketId("TEST"), generator.getNewTicketId("TEST"));
    }

    @Test
    public void verifySuffix() {
        final var suffix = "suffix";
        final var generator = new DefaultUniqueTicketIdGenerator(10, suffix);

        assertTrue(generator.getNewTicketId("test").endsWith(suffix));
    }

    @Test
    public void verifyNullSuffix() {
        final String nullSuffix = null;
        final var lengthWithoutSuffix = 23;
        final var generator = new DefaultUniqueTicketIdGenerator(12, nullSuffix);

        final var ticketId = generator.getNewTicketId("test");
        assertEquals(lengthWithoutSuffix, ticketId.length());
    }
}
