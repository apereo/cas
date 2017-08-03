package org.apereo.cas.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class DefaultUniqueTicketIdGeneratorTests {

    @Test
    public void verifyUniqueGenerationOfTicketIds() {
        final DefaultUniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator(10);

        assertNotSame(generator.getNewTicketId("TEST"), generator.getNewTicketId("TEST"));
    }

    @Test
    public void verifySuffix() {
        final String suffix = "suffix";
        final DefaultUniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator(10, suffix);

        assertTrue(generator.getNewTicketId("test").endsWith(suffix));
    }

    @Test
    public void verifyNullSuffix() {
        final String nullSuffix = null;
        final int lengthWithoutSuffix = 23;
        final DefaultUniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator(12, nullSuffix);

        final String ticketId = generator.getNewTicketId("test");
        assertEquals(lengthWithoutSuffix, ticketId.length());
    }
}
