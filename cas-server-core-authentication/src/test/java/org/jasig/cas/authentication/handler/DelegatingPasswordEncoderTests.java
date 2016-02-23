package org.jasig.cas.authentication.handler;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static junit.framework.Assert.assertEquals;

/**
 * @author Joe McCall
 */
public class DelegatingPasswordEncoderTests {

    private DelegatingPasswordEncoder passwordEncoder;

    @Before
    public void setup() {
        passwordEncoder = new DelegatingPasswordEncoder();
    }

    @Test
    public void testEncode() throws Exception {

        final String rawPassword = "expectedRawPassword";
        final String expectedEncodedPassword = "expectedEncodedPassword";
        MockPasswordEncoder mockPasswordEncoder = new MockPasswordEncoder();
        mockPasswordEncoder.setExpectedEncodedPassword(expectedEncodedPassword);
        passwordEncoder.setSpringPasswordEncoder(mockPasswordEncoder);

        assertEquals("password is encoded",
                expectedEncodedPassword,
                passwordEncoder.encode(rawPassword));

        assertEquals("collaborator has been called",
                1,
                mockPasswordEncoder.getEncodeCalledCount());
    }

    @Test
    public void testMatches() throws Exception {
        final Boolean expectedMatches = true;

        MockPasswordEncoder mockPasswordEncoder = new MockPasswordEncoder();
        mockPasswordEncoder.setExpectedMatches(expectedMatches);
        passwordEncoder.setSpringPasswordEncoder(mockPasswordEncoder);

        assertEquals("password matches as expected",
                (Object) expectedMatches,
                passwordEncoder.matches("", ""));

        assertEquals("collaborator has been called",
                1,
                mockPasswordEncoder.getMatchesCalledCount());
    }

    /**
     * Simple mock class to test collaboration with the spring password encoder
     */
    private class MockPasswordEncoder implements PasswordEncoder {

        private String expectedEncodedPassword;
        private Boolean expectedMatches;

        private int encodeCalledCount = 0;
        private int matchesCalledCount = 0;

        @Override
        public String encode(CharSequence rawPassword) {
            ++encodeCalledCount;
            return expectedEncodedPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            ++matchesCalledCount;
            return expectedMatches;
        }

        public void setExpectedEncodedPassword(String expectedEncodedPassword) {
            this.expectedEncodedPassword = expectedEncodedPassword;
        }

        public int getEncodeCalledCount() {
            return encodeCalledCount;
        }

        public void setExpectedMatches(Boolean expectedMatches) {
            this.expectedMatches = expectedMatches;
        }

        public int getMatchesCalledCount() {
            return matchesCalledCount;
        }
    }
}