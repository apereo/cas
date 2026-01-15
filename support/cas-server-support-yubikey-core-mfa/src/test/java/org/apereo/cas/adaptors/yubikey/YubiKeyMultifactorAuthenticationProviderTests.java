package org.apereo.cas.adaptors.yubikey;

import module java.base;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.HttpMessage;
import com.yubico.client.v2.YubicoClient;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link YubiKeyMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFAProvider")
class YubiKeyMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {

    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() throws Exception {
        val client = mock(YubicoClient.class);
        when(client.getWsapiUrls()).thenReturn(new String[]{"http://localhost:1234"});
        val http = mock(HttpClient.class);
        when(http.sendMessageToEndPoint(any(URL.class))).thenReturn(new HttpMessage(new URI("http://localhost:1234").toURL(), "message"));
        return new YubiKeyMultifactorAuthenticationProvider(client, http);
    }

    @Test
    void verifyFails() throws Throwable {
        val client = mock(YubicoClient.class);
        when(client.getWsapiUrls()).thenThrow(new RuntimeException());
        val http = mock(HttpClient.class);
        when(http.sendMessageToEndPoint(any(URL.class))).thenReturn(new HttpMessage(new URI("http://localhost:1234").toURL(), "message"));
        val provider = new YubiKeyMultifactorAuthenticationProvider(client, http);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(provider.isAvailable(service));
    }

}
