package org.apereo.cas.authentication.support.password;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RejectResultCodePasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("PasswordOps")
public class RejectResultCodePasswordPolicyHandlingStrategyTests {

    @Test
    public void verifyOperation() {
        val s = new RejectResultCodePasswordPolicyHandlingStrategy<>();
        assertFalse(s.supports(null));
        val response = new Object();
        assertFalse(s.supports(response));
        assertTrue(s.getAuthenticationResponseResultCodes(new Object()).isEmpty());
    }
}
