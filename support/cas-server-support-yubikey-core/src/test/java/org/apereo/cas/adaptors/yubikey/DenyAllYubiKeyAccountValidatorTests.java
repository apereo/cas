package org.apereo.cas.adaptors.yubikey;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is {@link DenyAllYubiKeyAccountValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DenyAllYubiKeyAccountValidatorTests {
    @Test
    public void verifyAction() {
        val v = new DenyAllYubiKeyAccountValidator();
        assertFalse(v.isValid("anything", "anything"));
    }
}
