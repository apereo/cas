package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyUniqueTicketIdGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
public class GroovyUniqueTicketIdGeneratorTests {
    @Test
    public void verifyOperation() {
        val resource = new ClassPathResource("GroovyUniqueTicketIdGenerator.groovy");
        val gen = new GroovyUniqueTicketIdGenerator(resource);
        assertEquals("sys666", gen.getNewTicketId("SYS"));
    }
}
