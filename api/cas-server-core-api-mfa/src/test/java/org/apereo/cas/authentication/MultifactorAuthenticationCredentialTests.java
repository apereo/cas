package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
public class MultifactorAuthenticationCredentialTests {

    @Test
    public void verifyOperation() {
        val input = new MultifactorAuthenticationCredential() {
            private static final long serialVersionUID = -7854668847716061700L;

            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }
        };
        input.setProviderId("nothing");
        assertNull(input.getProviderId());
    }

}
