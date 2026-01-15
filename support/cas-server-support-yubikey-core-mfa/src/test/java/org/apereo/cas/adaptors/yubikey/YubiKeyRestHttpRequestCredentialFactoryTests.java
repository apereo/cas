package org.apereo.cas.adaptors.yubikey;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFAProvider")
class YubiKeyRestHttpRequestCredentialFactoryTests {
    @Test
    void verifyAction() {
        val factory = new YubiKeyRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        body.add(YubiKeyRestHttpRequestCredentialFactory.PARAMETER_NAME_YUBIKEY_OTP, "token");
        assertFalse(factory.fromRequest(null, body).isEmpty());
    }

    @Test
    void verifyEmptyBody() {
        val factory = new YubiKeyRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        assertTrue(factory.fromRequest(null, body).isEmpty());
        body.put("some-other-key", List.of("value1"));
        assertTrue(factory.fromRequest(null, body).isEmpty());
    }
}
