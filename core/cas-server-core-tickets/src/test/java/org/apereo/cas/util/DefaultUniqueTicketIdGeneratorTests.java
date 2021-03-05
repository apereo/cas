package org.apereo.cas.util;

import org.apereo.cas.util.gen.DefaultLongNumericGenerator;
import org.apereo.cas.util.gen.HexRandomStringGenerator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
public class DefaultUniqueTicketIdGeneratorTests {

    @Test
    public void verifyUniqueGenerationOfTicketIds() {
        val generator = new DefaultUniqueTicketIdGenerator(10);
        assertNotSame(generator.getNewTicketId("TEST"), generator.getNewTicketId("TEST"));
    }

    @Test
    public void verifyUniqueGeneration() {
        val generator = new DefaultUniqueTicketIdGenerator(
            new DefaultLongNumericGenerator(8),
            new HexRandomStringGenerator(16), "suffix");
        assertNotSame(generator.getNewTicketId("TEST"), generator.getNewTicketId("TEST"));
    }

    @Test
    public void verifySuffix() {
        val suffix = "suffix";
        val generator = new DefaultUniqueTicketIdGenerator(10, suffix);

        assertTrue(generator.getNewTicketId("test").endsWith('-' + suffix));
    }

    @Test
    public void verifyNullSuffix() {
        val lengthWithoutSuffix = 23;
        val generator = new DefaultUniqueTicketIdGenerator(12, null);
        val ticketId = generator.getNewTicketId("test");

        assertEquals(lengthWithoutSuffix, ticketId.length());
    }
}
