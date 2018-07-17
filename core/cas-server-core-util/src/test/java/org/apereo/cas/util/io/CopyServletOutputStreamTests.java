package org.apereo.cas.util.io;

import lombok.val;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link CopyServletOutputStreamTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CopyServletOutputStreamTests {
    @Test
    public void verifyAction() throws Exception {
        val out = new ByteArrayOutputStream();
        val s = new CopyServletOutputStream(out);
        s.write("Test".getBytes(StandardCharsets.UTF_8));
        assertNotNull(s.getCopy());
        assertNotNull(s.getStringCopy());
        assertNotNull(out.toByteArray());
        assertTrue(s.isReady());
    }
}
