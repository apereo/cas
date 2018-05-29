package org.apereo.cas.adaptors.yubikey;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link AcceptAllYubiKeyAccountValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AcceptAllYubiKeyAccountValidatorTests {
    @Test
    public void verifyAction() {
        final var v = new AcceptAllYubiKeyAccountValidator();
        assertTrue(v.isValid("anything", "anything"));
    }
}
