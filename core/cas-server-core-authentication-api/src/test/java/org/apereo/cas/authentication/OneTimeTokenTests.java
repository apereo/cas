package org.apereo.cas.authentication;

import module java.base;
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
class OneTimeTokenTests {

    @Test
    void verifyComparisonWorks() {
        val otp1 = new OneTimeToken(123456, "casuser").assignIdIfNecessary();
        val otp2 = new OneTimeToken(123456, "casuser").assignIdIfNecessary();
        otp2.setIssuedDateTime(otp1.getIssuedDateTime().plusSeconds(1));
        assertEquals(-1, otp1.compareTo(otp2));
        assertNotEquals(0, otp1.getId());
    }
}
