package org.apereo.cas.authentication;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
class MultifactorAuthenticationCredentialTests {

    @Test
    void verifyOperation() throws Throwable {
        val input = new MultifactorAuthenticationCredential() {
            @Serial
            private static final long serialVersionUID = -7854668847716061700L;

            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }

            @Override
            public CredentialMetadata getCredentialMetadata() {
                return mock(CredentialMetadata.class);
            }
        };
        input.setProviderId("nothing");
        assertNull(input.getProviderId());
    }

}
