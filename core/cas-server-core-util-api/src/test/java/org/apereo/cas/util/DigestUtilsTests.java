package org.apereo.cas.util;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DigestUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
public class DigestUtilsTests {
    @Test
    public void verifySha() {
        assertNotNull(DigestUtils.sha("HelloWorld".getBytes(StandardCharsets.UTF_8)));
        assertNotNull(DigestUtils.shaBase64("salt", "HelloWorld"));
        assertNotNull(DigestUtils.shaBase64("salt", "HelloWorld", ";", true));
        assertNotNull(DigestUtils.shaBase32("salt", "HelloWorld", ";", true));
        assertNotNull(DigestUtils.shaBase64("salt", "HelloWorld", ";"));
        assertThrows(SecurityException.class, () -> DigestUtils.rawDigestSha256(null));
        assertThrows(SecurityException.class, () -> DigestUtils.rawDigest(null, ArrayUtils.EMPTY_BYTE_ARRAY));
        assertThrows(SecurityException.class, () -> DigestUtils.rawDigest(null, null, ArrayUtils.EMPTY_STRING_ARRAY));
    }
}
