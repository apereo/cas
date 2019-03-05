package org.apereo.cas.util.crypto;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This tests {@link GlibcCryptPasswordEncoder}.
 *
 * @author Martin Böhmer
 * @since 5.3.10
 */
@Slf4j
public class GlibcCryptPasswordEncoderTests {

    @Test
    public void testSha512Encoding() {
        testEncoding("SHA-512");
        testEncoding("6");
    }

    @Test
    public void testSha256Encoding() {
        testEncoding("SHA-256");
        testEncoding("5");
    }
    
    @Test
    public void testMd5Encoding() {
        testEncoding("MD5");
        testEncoding("1");
    }
    
    @Test
    public void testDesUnixCryptEncoding() {
        testEncoding("aB");
        testEncoding("42xyz");
        testEncoding("");
    }

    private void testEncoding(final String algorithm) {
        final String passwordClear = "12345abcDEF!$";
        final GlibcCryptPasswordEncoder encoder = new GlibcCryptPasswordEncoder(algorithm, 0, null);
        // Encode
        final String passwordHash = encoder.encode(passwordClear);
        LOGGER.debug("Password [{}] was encoded by algorithm [{}] to hash [{}]", passwordClear, algorithm, passwordHash);
        // Match
        final boolean match = encoder.matches(passwordClear, passwordHash);
        LOGGER.debug("Encoded password [{}] matches original password [{}]: {}", passwordClear, passwordHash, match);
        // Check
        assertTrue(match);
    }

}
