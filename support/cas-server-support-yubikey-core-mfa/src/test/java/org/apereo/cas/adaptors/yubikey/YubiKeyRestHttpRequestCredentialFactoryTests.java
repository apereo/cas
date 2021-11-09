package org.apereo.cas.adaptors.yubikey;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFAProvider")
public class YubiKeyRestHttpRequestCredentialFactoryTests {
    @Test
    public void verifyAction() {
        val f = new YubiKeyRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        body.add(YubiKeyRestHttpRequestCredentialFactory.PARAMETER_NAME_YUBIKEY_OTP, "token");
        assertFalse(f.fromRequest(null, body).isEmpty());
    }

    @Test
    public void verifyEmptyBody() {
        val f = new YubiKeyRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        assertTrue(f.fromRequest(null, body).isEmpty());
        body.put("some-other-key", List.of("value1"));
        assertTrue(f.fromRequest(null, body).isEmpty());
    }
}
