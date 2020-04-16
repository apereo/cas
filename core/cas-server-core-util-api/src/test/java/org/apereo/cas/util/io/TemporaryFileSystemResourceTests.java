package org.apereo.cas.util.io;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TemporaryFileSystemResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("FileSystem")
public class TemporaryFileSystemResourceTests {

    @Test
    public void verifyOperation() throws Exception {
        val resource = new TemporaryFileSystemResource(File.createTempFile("temp", ".txt"));
        assertFalse(resource.isFile());

        try (val is = resource.getInputStream()) {
            val results = IOUtils.toString(is, StandardCharsets.UTF_8);
            assertNotNull(results);
        }
        assertFalse(resource.exists());
    }

    @Test
    public void verifyChannel() throws Exception {
        val resource = new TemporaryFileSystemResource(File.createTempFile("temp2", ".txt"));
        try (val channel = resource.readableChannel()) {
            assertTrue(channel.isOpen());
            val c = channel.read(ByteBuffer.allocate(1));
            assertEquals(-1, c);
        }
        assertFalse(resource.exists());
    }
}
