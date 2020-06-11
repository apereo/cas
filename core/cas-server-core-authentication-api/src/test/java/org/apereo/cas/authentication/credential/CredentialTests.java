package org.apereo.cas.authentication.credential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class CredentialTests {

    @Test
    public void verifyCred() {
        val c = new AbstractCredential() {
            private static final long serialVersionUID = -1746359565306558329L;

            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }
        };
        assertNotNull(c.getCredentialClass());
        assertTrue(c.isValid());
    }

    @Test
    public void verifyValid() {
        val c = new AbstractCredential() {
            private static final long serialVersionUID = -1746359565306558329L;

            @Override
            public String getId() {
                return null;
            }
        };
        val context = mock(ValidationContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                c.validate(context);
            }
        });
    }

}
