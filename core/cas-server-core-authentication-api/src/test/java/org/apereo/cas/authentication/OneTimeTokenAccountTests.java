package org.apereo.cas.authentication;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OneTimeTokenAccountTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("MFA")
public class OneTimeTokenAccountTests {

    @Test
    public void verifyComparisonWorks() {
        val otp1 = OneTimeTokenAccount.builder()
            .username("casuser")
            .secretKey("secret")
            .validationCode(123456)
            .name(UUID.randomUUID().toString())
            .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
            .build();

        val otp2 = OneTimeTokenAccount.builder()
            .username("casuser")
            .secretKey("secret")
            .validationCode(987063)
            .name(otp1.getName())
            .scratchCodes(CollectionUtils.wrapList(1, 7, 3, 4, 2, 9))
            .build();

        assertNotEquals(0, otp1.compareTo(otp2));
        assertEquals(0, otp1.compareTo(otp1.clone()));
    }
}
