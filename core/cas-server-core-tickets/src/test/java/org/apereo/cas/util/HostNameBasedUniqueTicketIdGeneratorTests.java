package org.apereo.cas.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link HostNameBasedUniqueTicketIdGenerator}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public class HostNameBasedUniqueTicketIdGeneratorTests {
    
    @Test
    public void verifyUniqueGenerationOfTicketIds() {
        final var generator = new HostNameBasedUniqueTicketIdGenerator(10, StringUtils.EMPTY);
        final var id1 = generator.getNewTicketId("TEST");
        final var id2 = generator.getNewTicketId("TEST");
        assertNotSame(id1, id2);
    }
}
