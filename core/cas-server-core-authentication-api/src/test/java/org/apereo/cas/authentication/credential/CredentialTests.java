package org.apereo.cas.authentication.credential;

import module java.base;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class CredentialTests {

    private static AbstractCredential getCredential() {
        return new AbstractCredential() {
            @Serial
            private static final long serialVersionUID = -1746359565306558329L;

            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }

            @Override
            public CredentialMetadata getCredentialMetadata() {
                return new BasicCredentialMetadata(this);
            }
        };
    }

    @Test
    void verifyCred() {
        val credential = getCredential();
        assertNotNull(credential.getCredentialMetadata().getCredentialClass());
        assertTrue(credential.isValid());
    }

    @Test
    void verifyEquals() {
        val credential = getCredential();
        val credential2 = getCredential();
        assertNotNull(credential.getCredentialMetadata().getCredentialClass());
        assertTrue(credential.isValid());
        assertNotEquals(credential2, credential);
        assertEquals(credential, credential);
    }

    @Test
    void verifyValid() throws Throwable {
        val credential = new AbstractCredential() {
            @Serial
            private static final long serialVersionUID = -1746359565306558329L;

            @Override
            public String getId() {
                return null;
            }
        };
        val context = mock(ValidationContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        assertDoesNotThrow(() -> credential.validate(context));
    }

}
