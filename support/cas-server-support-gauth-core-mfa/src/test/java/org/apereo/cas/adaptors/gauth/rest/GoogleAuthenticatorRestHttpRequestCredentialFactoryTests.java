package org.apereo.cas.adaptors.gauth.rest;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.Assert.*;

/**
 * This is {@link GoogleAuthenticatorRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GoogleAuthenticatorRestHttpRequestCredentialFactoryTests {

    @Test
    public void verifyNoCredentials() {
        final var f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        final var body = new LinkedMultiValueMap<String, String>();
        final var results = f.fromRequestBody(body);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyCredentials() {
        final var f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        final var body = new LinkedMultiValueMap<String, String>();
        body.add(GoogleAuthenticatorRestHttpRequestCredentialFactory.PARAMETER_NAME_GAUTH_OTP, "132456");
        final var results = f.fromRequestBody(body);
        assertFalse(results.isEmpty());
        assertEquals("132456", results.get(0).getId());
    }
}
