package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.util.http.HttpClient;

import com.yubico.client.v2.YubicoClient;
import lombok.val;
import org.junit.jupiter.api.Tag;

import static org.mockito.Mockito.*;

/**
 * This is {@link YubiKeyMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
public class YubiKeyMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        val client = mock(YubicoClient.class);
        val http = mock(HttpClient.class);
        return new YubiKeyMultifactorAuthenticationProvider(client, http);
    }
}
