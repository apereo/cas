package org.apereo.cas;

import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultMessageDescriptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
public class DefaultMessageDescriptorTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultMessageDescriptorTests.json");

    @Test
    public void verifySerialization() throws Exception {
        val d = new DefaultMessageDescriptor("sample.code");
        MAPPER.writeValue(JSON_FILE, d);
        val read = MAPPER.readValue(JSON_FILE, MessageDescriptor.class);
        assertEquals(d, read);
    }
}
