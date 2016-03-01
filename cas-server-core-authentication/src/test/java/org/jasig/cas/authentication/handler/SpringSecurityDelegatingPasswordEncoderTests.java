package org.jasig.cas.authentication.handler;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.*;

/**
 * @author Joe McCall
 * @since 4.3
 */
public class SpringSecurityDelegatingPasswordEncoderTests {

    private SpringSecurityDelegatingPasswordEncoder passwordEncoder;

    @Before
    public void setup() {
        passwordEncoder = new SpringSecurityDelegatingPasswordEncoder();
    }

    @Test
    public void verifyEncode() throws Exception {

        final String rawPassword = "expectedRawPassword";
        final String expectedEncodedPassword = "expectedEncodedPassword";
        final MockPasswordEncoder mockPasswordEncoder = new MockPasswordEncoder();
        mockPasswordEncoder.setExpectedEncodedPassword(expectedEncodedPassword);
        passwordEncoder.setDelegate(mockPasswordEncoder);

        assertEquals("password is encoded",
                expectedEncodedPassword,
                passwordEncoder.encode(rawPassword));

        assertEquals("collaborator has been called",
                1,
                mockPasswordEncoder.getEncodeCalledCount());
    }

    @Test
    public void verifyMatches() throws Exception {
        final Boolean expectedMatches = true;

        final MockPasswordEncoder mockPasswordEncoder = new MockPasswordEncoder();
        mockPasswordEncoder.setExpectedMatches(expectedMatches);
        passwordEncoder.setDelegate(mockPasswordEncoder);

        assertEquals("password matches as expected",
                expectedMatches,
                passwordEncoder.matches("", ""));

        assertEquals("collaborator has been called",
                1,
                mockPasswordEncoder.getMatchesCalledCount());
    }

    /**
     * Simple mock class to test collaboration with the spring password encoder
     */
    private static class MockPasswordEncoder implements PasswordEncoder {

        private String expectedEncodedPassword;
        private Boolean expectedMatches;

        private int encodeCalledCount;
        private int matchesCalledCount;

        @Override
        public String encode(final CharSequence rawPassword) {
            ++encodeCalledCount;
            return expectedEncodedPassword;
        }

        @Override
        public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
            ++matchesCalledCount;
            return expectedMatches;
        }

        public void setExpectedEncodedPassword(final String expectedEncodedPassword) {
            this.expectedEncodedPassword = expectedEncodedPassword;
        }

        public int getEncodeCalledCount() {
            return encodeCalledCount;
        }

        public void setExpectedMatches(final Boolean expectedMatches) {
            this.expectedMatches = expectedMatches;
        }

        public int getMatchesCalledCount() {
            return matchesCalledCount;
        }
    }
}
