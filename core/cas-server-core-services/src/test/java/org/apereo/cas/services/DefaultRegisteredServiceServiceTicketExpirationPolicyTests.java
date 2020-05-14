package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceServiceTicketExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class DefaultRegisteredServiceServiceTicketExpirationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultRegisteredServiceServiceTicketExpirationPolicyTests.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializationToJson() throws IOException {
        val p = new DefaultRegisteredServiceServiceTicketExpirationPolicy();
        p.setNumberOfUses(12);
        p.setTimeToLive("60");
        MAPPER.writeValue(JSON_FILE, p);
        val repositoryRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceServiceTicketExpirationPolicy.class);
        assertEquals(p, repositoryRead);
    }

    @Test
    public void verifyUndefined() {
        val p = RegisteredServiceServiceTicketExpirationPolicy.undefined();
        assertNull(p.getTimeToLive());
        assertEquals(Long.MIN_VALUE, p.getNumberOfUses());
    }
}
