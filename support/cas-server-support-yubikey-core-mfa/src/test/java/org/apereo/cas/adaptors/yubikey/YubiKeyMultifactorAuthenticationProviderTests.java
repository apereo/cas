package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;

import com.yubico.client.v2.YubicoClient;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link YubiKeyMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFAProvider")
public class YubiKeyMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {

    @Override
    @SneakyThrows
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        val client = mock(YubicoClient.class);
        when(client.getWsapiUrls()).thenReturn(new String[]{"http://localhost:1234"});
        val http = mock(HttpClient.class);
        when(http.sendMessageToEndPoint(any(URL.class))).thenReturn(new HttpMessage(new URL("http://localhost:1234"), "message"));
        return new YubiKeyMultifactorAuthenticationProvider(client, http);
    }

    @Test
    public void verifyFails() throws Exception {
        val client = mock(YubicoClient.class);
        when(client.getWsapiUrls()).thenThrow(new RuntimeException());
        val http = mock(HttpClient.class);
        when(http.sendMessageToEndPoint(any(URL.class))).thenReturn(new HttpMessage(new URL("http://localhost:1234"), "message"));
        val provider = new YubiKeyMultifactorAuthenticationProvider(client, http);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(provider.isAvailable(service));
    }

}
