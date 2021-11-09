package org.apereo.cas.gauth.rest;

import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFAProvider")
public class GoogleAuthenticatorRestHttpRequestCredentialFactoryTests {

    @Test
    public void verifyNoCredentials() {
        val f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        val results = f.fromRequest(new MockHttpServletRequest(), body);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyCredentials() {
        val f = new GoogleAuthenticatorRestHttpRequestCredentialFactory();
        val body = new LinkedMultiValueMap<String, String>();
        body.add(GoogleAuthenticatorRestHttpRequestCredentialFactory.PARAMETER_NAME_GAUTH_OTP, "132456");
        body.add(GoogleAuthenticatorRestHttpRequestCredentialFactory.PARAMETER_NAME_GAUTH_ACCT, "132456");
        val results = f.fromRequest(new MockHttpServletRequest(), body);
        assertFalse(results.isEmpty());
        val credential = (GoogleAuthenticatorTokenCredential) results.get(0);
        assertEquals("132456", credential.getId());
        assertEquals(132456, credential.getAccountId());
    }
}
