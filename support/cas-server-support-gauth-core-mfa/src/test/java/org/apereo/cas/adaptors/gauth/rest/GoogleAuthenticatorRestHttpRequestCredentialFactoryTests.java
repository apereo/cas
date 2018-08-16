package org.apereo.cas.adaptors.gauth.rest;

import org.apereo.cas.authentication.Credential;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

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
        final GoogleAuthenticatorRestHttpRequestCredentialFactory f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        final LinkedMultiValueMap body = new LinkedMultiValueMap<String, String>();
        final List<Credential> results = f.fromRequest(null, body);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyCredentials() {
        final GoogleAuthenticatorRestHttpRequestCredentialFactory f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        final LinkedMultiValueMap body = new LinkedMultiValueMap<>();
        body.add(GoogleAuthenticatorRestHttpRequestCredentialFactory.PARAMETER_NAME_GAUTH_OTP, "132456");
        final List<Credential> results = f.fromRequest(null, body);
        assertFalse(results.isEmpty());
        assertEquals("132456", results.get(0).getId());
    }
}
