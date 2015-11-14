package org.jasig.cas.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link org.jasig.cas.util.HostNameBasedUniqueTicketIdGenerator}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class HostNameBasedUniqueTicketIdGeneratorTests {

    @Test
    public void verifyUniqueGenerationOfTicketIds() throws Exception {
        final HostNameBasedUniqueTicketIdGenerator generator = new HostNameBasedUniqueTicketIdGenerator(10);
        final String id1 = generator.getNewTicketId("TEST");
        final String id2 = generator.getNewTicketId("TEST");
        assertNotSame(id1, id2);
    }
}
