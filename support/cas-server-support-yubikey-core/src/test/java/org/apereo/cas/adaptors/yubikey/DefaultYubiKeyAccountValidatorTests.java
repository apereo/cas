package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultYubiKeyAccountValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFAProvider")
@SuppressWarnings("JavaUtilDate")
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

    @Test
    public void verifyActionFailsStatus() throws Exception {
        val client = mock(YubicoClient.class);
        val r = mock(VerificationResponse.class);
        when(client.verify(anyString())).thenReturn(r);
        when(r.getStatus()).thenReturn(ResponseStatus.REPLAYED_REQUEST);
        when(r.getTimestamp()).thenReturn(String.valueOf(new Date().getTime()));
        val v = new DefaultYubiKeyAccountValidator(client);
        assertFalse(v.isValid("casuser", "cccccccvlidcrkrrculeevnlcjbngciggidutebbkjrv"));
    }

    @Test
    public void verifyBadPubKey() {
        val client = mock(YubicoClient.class);
        val v = new DefaultYubiKeyAccountValidator(client);
        assertFalse(v.isValid("casuser", "abcdeyf"));
    }

    @Test
    public void verifyNoPubKey() {
        val client = mock(YubicoClient.class);
        val v = new DefaultYubiKeyAccountValidator(client) {
            @Override
            public String getTokenPublicId(final String token) {
                return null;
            }
        };
        assertFalse(v.isValid("casuser", "abcdeyf"));
    }
}
