package org.apereo.cas.util.crypto;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This tests {@link GlibcCryptPasswordEncoder}.
 *
 * @author Martin Böhmer
 * @since 5.3.10
 */
@Slf4j
@Tag("Simple")
public class GlibcCryptPasswordEncoderTests {

    private static final String PASSWORD_CLEAR = "12345abcDEF!$";

    private static boolean testEncodingRoundtrip(final String algorithm) {
        val encoder = new GlibcCryptPasswordEncoder(algorithm, 0, null);

        val passwordHash = encoder.encode(PASSWORD_CLEAR);
        LOGGER.debug("Password [{}] was encoded by algorithm [{}] to hash [{}]", PASSWORD_CLEAR, algorithm, passwordHash);

        val match = encoder.matches(PASSWORD_CLEAR, passwordHash);
        LOGGER.debug("Does password [{}] match original password [{}]: [{}]", passwordHash, PASSWORD_CLEAR, match);

        return match;
    }

    private static boolean testMatchWithDifferentSalt(final String algorithm, final String encodedPassword) {
        val encoder = new GlibcCryptPasswordEncoder(algorithm, 0, null);
        val match = encoder.matches(PASSWORD_CLEAR, encodedPassword);
        LOGGER.debug("Does password [{}] match original password [{}]: [{}]", encodedPassword, PASSWORD_CLEAR, match);
        return match;
    }

    @Test
    public void sha512EncodingTest() {
        assertTrue(testEncodingRoundtrip("SHA-512"));
        assertTrue(testEncodingRoundtrip("6"));
        assertTrue(testMatchWithDifferentSalt("SHA-512", "$6$rounds=1000$df273de606d3609a$GAPcq.K4jO3KkfusCM7Zr8Cci4qf.jOsWj5hkGBpwRg515bKk93afAXHy/lg.2LPr8ZItHoR3AR5X3XOXndZI0"));
    }

    @Test
    public void ha256EncodingTest() {
        assertTrue(testEncodingRoundtrip("SHA-256"));
        assertTrue(testEncodingRoundtrip("5"));
        assertTrue(testMatchWithDifferentSalt("SHA-256", "$5$rounds=1000$e98244bb01b64f47$2qphrK8axtGjgmCJFYwaH7czw5iK9feLl7tKjyTlDy0"));
    }

    @Test
    public void md5EncodingTest() {
        assertTrue(testEncodingRoundtrip("MD5"));
        assertTrue(testEncodingRoundtrip("1"));
        assertTrue(testMatchWithDifferentSalt("MD5", "$1$c4676fd0$HOHZ2CYp45lZAAQyvF4C21"));
    }

    @Test
    public void desUnixCryptEncodingTest() {
        assertTrue(testEncodingRoundtrip("aB"));
        assertTrue(testEncodingRoundtrip("42xyz"));
        assertTrue(testMatchWithDifferentSalt("aB", "aB4fMcNOggJoQ"));
    }

}
