package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OneTimeTokenTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
public class OneTimeTokenTests {
    @Test
    public void verifyComparisonWorks() {
        val otp1 = new OneTimeToken(123456, "casuser");
        val otp2 = new OneTimeToken(123456, "casuser");
        assertEquals(-1, otp1.compareTo(otp2));
        assertNotEquals(0, otp1.getId());
    }
}
