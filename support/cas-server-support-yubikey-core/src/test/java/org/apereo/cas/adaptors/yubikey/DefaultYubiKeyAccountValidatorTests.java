package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import org.junit.Test;

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
        final var client = mock(YubicoClient.class);
        final var r = mock(VerificationResponse.class);
        when(client.verify(anyString())).thenReturn(r);
        when(r.getStatus()).thenReturn(ResponseStatus.OK);
        when(r.getTimestamp()).thenReturn(String.valueOf(new Date().getTime()));
        final var v = new DefaultYubiKeyAccountValidator(client);
        assertTrue(v.isValid("casuser", "cccccccvlidcrkrrculeevnlcjbngciggidutebbkjrv"));
    }
}
