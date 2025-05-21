package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OneTimeTokenTests}.
 * Sleeping for one millisecond because Windows tests fail sometimes, presumably because time is the same.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
class OneTimeTokenTests {

    @Test
    void verifyComparisonWorks() throws Throwable {
        val otp1 = new OneTimeToken(123456, "casuser").assignIdIfNecessary();
        Thread.sleep(1);
        val otp2 = new OneTimeToken(123456, "casuser").assignIdIfNecessary();
        assertEquals(-1, otp1.compareTo(otp2));
        assertNotEquals(0, otp1.getId());
    }
}
