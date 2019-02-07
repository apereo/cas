package org.apereo.cas.gauth.rest;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GoogleAuthenticatorRestHttpRequestCredentialFactoryTests {

    @Test
    public void verifyNoCredentials() {
        val f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        val results = f.fromRequest(null, body);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyCredentials() {
        val f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        body.add(GoogleAuthenticatorRestHttpRequestCredentialFactory.PARAMETER_NAME_GAUTH_OTP, "132456");
        val results = f.fromRequest(null, body);
        assertFalse(results.isEmpty());
        assertEquals("132456", results.get(0).getId());
    }
}
