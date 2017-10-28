package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link HostNameBasedUniqueTicketIdGenerator}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class HostNameBasedUniqueTicketIdGeneratorTests {

    @Test
    public void verifyUniqueGenerationOfTicketIds() {
        final HostNameBasedUniqueTicketIdGenerator generator = new HostNameBasedUniqueTicketIdGenerator(10, StringUtils.EMPTY);
        final String id1 = generator.getNewTicketId("TEST");
        final String id2 = generator.getNewTicketId("TEST");
        assertNotSame(id1, id2);
    }
}
