package org.jasig.cas.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class DefaultUniqueTicketIdGeneratorTests {

    @Test
    public void verifyUniqueGenerationOfTicketIds() {
        final DefaultUniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator(
            10);

        assertNotSame(generator.getNewTicketId("TEST"), generator
            .getNewTicketId("TEST"));
    }

    @Test
    public void verifySuffix() {
        final String suffix = "suffix";
        final DefaultUniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator(10, suffix);

        assertTrue(generator.getNewTicketId("test").endsWith(suffix));
    }
}
