package org.apereo.cas.adaptors.yubikey;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptAllYubiKeyAccountValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFAProvider")
class AcceptAllYubiKeyAccountValidatorTests {
    @Test
    void verifyAction() {
        val v = new AcceptAllYubiKeyAccountValidator();
        assertTrue(v.isValid("anything", "anything"));
    }
}
