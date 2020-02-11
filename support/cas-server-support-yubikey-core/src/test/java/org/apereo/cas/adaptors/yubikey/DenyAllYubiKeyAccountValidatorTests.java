package org.apereo.cas.adaptors.yubikey;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DenyAllYubiKeyAccountValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFA")
public class DenyAllYubiKeyAccountValidatorTests {
    @Test
    public void verifyAction() {
        val v = new DenyAllYubiKeyAccountValidator();
        assertFalse(v.isValid("anything", "anything"));
    }
}
