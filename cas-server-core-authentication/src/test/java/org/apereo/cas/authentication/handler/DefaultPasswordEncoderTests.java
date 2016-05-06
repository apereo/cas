package org.apereo.cas.authentication.handler;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class DefaultPasswordEncoderTests {

    public static final String TEST_STRING_MD5 = "1f3870be274f6c49b3e31a0c6728957f";
    public static final String TEST_STRING = "apple";

    private PasswordEncoder passwordEncoder = new DefaultPasswordEncoder("MD5");

    @Test
    public void verifyNullPassword() {
        assertEquals(null, this.passwordEncoder.encode(null));
    }

    @Test
    public void verifyMd5Hash() {
        assertEquals(TEST_STRING_MD5, this.passwordEncoder
            .encode(TEST_STRING));
    }

    @Test
    public void verifySha1Hash() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("SHA1");

        final String hash = pe.encode("this is a test");

        assertEquals("fa26be19de6bff93f70bc2308434e4a440bbad02", hash);

    }

    @Test
    public void verifySha1Hash2() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("SHA1");

        final String hash = pe.encode("TEST of the SYSTEM");

        assertEquals("82ae28dfad565dd9882b94498a271caa29025d5f", hash);

    }

    @Test
    public void verifyInvalidEncodingType() {
        final PasswordEncoder pe = new DefaultPasswordEncoder("scott");
        try {
            pe.encode("test");
            fail("exception expected.");
        } catch (final Exception e) {
            return;
        }
    }

    @Test
    public void verifyMatchesMethod() {
        assertTrue("matches with expected inputs", this.passwordEncoder.matches(TEST_STRING, TEST_STRING_MD5));
        assertTrue("matches with null inputs", passwordEncoder.matches(null, null));
        assertFalse("does not match with partial null inputs", passwordEncoder.matches(null, TEST_STRING_MD5));
        assertFalse("does not match with partial null inputs", passwordEncoder.matches(TEST_STRING, null));
        assertFalse("does not match with bad inputs", this.passwordEncoder.matches("wrong " + TEST_STRING, TEST_STRING_MD5));
    }
}
