package org.apereo.cas.adaptors.duo.rest;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityPasscodeCredential;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
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
@Tag("MFAProvider")
public class DuoSecurityRestHttpRequestCredentialFactoryTests {
    @Test
    public void verifyOperation() {
        val factory = new DuoSecurityRestHttpRequestCredentialFactory();
        val request = new MockHttpServletRequest();

        val body = new LinkedMultiValueMap<String, String>();
        assertTrue(factory.fromRequest(request, body).isEmpty());

        body.put(RestHttpRequestCredentialFactory.PARAMETER_USERNAME, List.of("user"));
        assertTrue(factory.fromRequest(request, body).isEmpty());

        body.put(DuoSecurityRestHttpRequestCredentialFactory.PARAMETER_NAME_PASSCODE, List.of("123456"));
        body.put(DuoSecurityRestHttpRequestCredentialFactory.PARAMETER_NAME_PROVIDER, List.of("custom-duo"));
        var credentials = factory.fromRequest(request, body);
        assertFalse(credentials.isEmpty());
        var credential = (DuoSecurityPasscodeCredential) credentials.get(0);
        assertEquals(credential.getProviderId(), "custom-duo");

        credentials = factory.fromAuthentication(request, body, CoreAuthenticationTestUtils.getAuthentication(),
            new TestMultifactorAuthenticationProvider());
        val directCredential = (DuoSecurityDirectCredential) credentials.get(0);
        assertEquals(TestMultifactorAuthenticationProvider.ID, directCredential.getProviderId());
        assertNotNull(directCredential.getPrincipal());
    }
}
