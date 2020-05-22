package org.apereo.cas.support.validation;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.token.UsernameToken;
import org.apache.wss4j.dom.validate.Credential;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CipheredCredentialsValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CipheredCredentialsValidatorTests {

    @Test
    public void verifyOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val validator = new CipheredCredentialsValidator(CipherExecutor.noOp());

        val credential = new Credential();
        val token = mock(UsernameToken.class);
        when(token.getName()).thenReturn(id);
        when(token.getPassword()).thenReturn(id);
        credential.setUsernametoken(token);
        assertNotNull(validator.validate(credential, mock(RequestData.class)));
    }

    @Test
    public void verifyFailsOperation() {
        val id = UUID.randomUUID().toString();
        val validator = new CipheredCredentialsValidator(CipherExecutor.noOp());
        val credential = new Credential();
        val token = mock(UsernameToken.class);
        when(token.getName()).thenReturn(id);
        when(token.getPassword()).thenReturn(UUID.randomUUID().toString());
        credential.setUsernametoken(token);
        assertThrows(WSSecurityException.class, () -> validator.validate(credential, mock(RequestData.class)));
    }
}
