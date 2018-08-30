
package org.apereo.cas.authentication;

import org.apereo.cas.util.CollectionUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link OneTimeTokenAccountTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.4
 */
public class OneTimeTokenAccountTests {

    @Test
    public void verifyComparisonWorks() {
        final OneTimeTokenAccount otp1 = new OneTimeTokenAccount("casuser", "secret", 123456,
            CollectionUtils.wrapList(1, 2, 3, 4, 5, 6));
        final OneTimeTokenAccount otp2 = new OneTimeTokenAccount("casuser", "secret", 987063,
            CollectionUtils.wrapList(1, 2, 1, 4, 7, 6));
        assertEquals(1, otp1.compareTo(otp2));
        assertEquals(0, otp1.compareTo(otp1.clone()));
    }
}
