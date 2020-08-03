package org.apereo.cas.gauth;

import org.apereo.cas.gauth.credential.DummyCredentialRepository;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFA")
public class GoogleAuthenticatorServiceTests {

    @Test
    public void verifyOperation() {
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        val authenticator = new GoogleAuthenticator(bldr.build());
        authenticator.setCredentialRepository(new DummyCredentialRepository());
        val googleAuth = new GoogleAuthenticatorService(authenticator);
        assertNotNull(googleAuth.getCredentialRepository());
        val key = googleAuth.createCredentials("casuser");
        assertNotNull(key);
        assertFalse(googleAuth.authorize(key.getKey(), key.getVerificationCode()));
    }
}
