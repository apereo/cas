package org.apereo.cas.authentication.credential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import java.io.Serial;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class CredentialTests {

    private static AbstractCredential getCredential() {
        return new AbstractCredential() {
            @Serial
            private static final long serialVersionUID = -1746359565306558329L;

            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }
        };
    }

    @Test
    public void verifyCred() {
        val c1 = getCredential();
        assertNotNull(c1.getCredentialClass());
        assertTrue(c1.isValid());
    }

    @Test
    public void verifyEquals() {
        val c1 = getCredential();
        val c2 = getCredential();
        assertNotNull(c1.getCredentialClass());
        assertTrue(c1.isValid());
        assertTrue(Math.abs(c1.hashCode()) > 0);
        assertNotEquals(c2, c1);
        assertEquals(c1, c1);
    }

    @Test
    public void verifyValid() {
        val c = new AbstractCredential() {
            @Serial
            private static final long serialVersionUID = -1746359565306558329L;

            @Override
            public String getId() {
                return null;
            }
        };
        val context = mock(ValidationContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        assertDoesNotThrow(() -> c.validate(context));
    }

}
