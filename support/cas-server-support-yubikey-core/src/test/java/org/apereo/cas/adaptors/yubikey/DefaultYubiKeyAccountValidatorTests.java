package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultYubiKeyAccountValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultYubiKeyAccountValidatorTests {
    @Test
    public void verifyAction() throws Exception {
        val client = mock(YubicoClient.class);
        val r = mock(VerificationResponse.class);
        when(client.verify(anyString())).thenReturn(r);
        when(r.getStatus()).thenReturn(ResponseStatus.OK);
        when(r.getTimestamp()).thenReturn(String.valueOf(new Date().getTime()));
        val v = new DefaultYubiKeyAccountValidator(client);
        assertTrue(v.isValid("casuser", "cccccccvlidcrkrrculeevnlcjbngciggidutebbkjrv"));
    }
}
