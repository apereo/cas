package org.apereo.cas.adaptors.yubikey;

import lombok.val;
import org.junit.jupiter.api.Test;
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
        val f = new YubiKeyRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        body.add(YubiKeyRestHttpRequestCredentialFactory.PARAMETER_NAME_YUBIKEY_OTP, "token");
        assertFalse(f.fromRequest(null, body).isEmpty());
    }
}
