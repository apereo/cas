package org.jasig.cas.authentication.handler;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class PlainTextPasswordEncoderTests {

    private static final String CONST_TO_ENCODE = "CAS IS COOL";

    private final PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();

    @Test
    public void verifyNullValueToTranslate() {
        assertEquals(null, this.passwordEncoder.encode(null));
    }

    @Test
    public void verifyValueToTranslate() {
        assertEquals(CONST_TO_ENCODE, this.passwordEncoder.encode(CONST_TO_ENCODE));
    }
}
