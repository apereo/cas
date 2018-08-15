package org.apereo.cas.adaptors.yubikey;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.Assert.*;

/**
 * This is {@link YubiKeyRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class YubiKeyRestHttpRequestCredentialFactoryTests {
    @Test
    public void verifyAction() {
        final YubiKeyRestHttpRequestCredentialFactory f = new YubiKeyRestHttpRequestCredentialFactory();
        final LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(YubiKeyRestHttpRequestCredentialFactory.PARAMETER_NAME_YUBIKEY_OTP, "token");
        assertFalse(f.fromRequest(null, body).isEmpty());
    }
}
