package org.apereo.cas.adaptors.duo.rest;

import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFA")
public class DuoSecurityRestHttpRequestCredentialFactoryTests {
    @Test
    public void verifyOperation() {
        val factory = new DuoSecurityRestHttpRequestCredentialFactory();
        val request = new MockHttpServletRequest();
        val body = new LinkedMultiValueMap<String, String>();
        assertTrue(factory.fromRequest(request, body).isEmpty());
        body.put(RestHttpRequestCredentialFactory.PARAMETER_USERNAME, List.of("user"));
        assertThrows(IllegalArgumentException.class, () -> factory.fromRequest(request, body));
        body.put(DuoSecurityRestHttpRequestCredentialFactory.PARAMETER_NAME_OTP, List.of("123456"));
        assertFalse(factory.fromRequest(request, body).isEmpty());
    }
}
