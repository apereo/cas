package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class MultifactorAuthenticationCredentialTests {

    @Test
    public void verifyOperation() {
        val input = new MultifactorAuthenticationCredential() {
        };
        input.setProviderId("nothing");
        assertNull(input.getProviderId());
    }

}
