package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorTokenCredentialRepositoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */

@Import(BaseGoogleAuthenticatorTests.SharedTestConfiguration.class)
@TestPropertySource(properties = "management.endpoint.gauthCredentialRepository.enabled=true")
@Getter
@Tag("MFA")
public class GoogleAuthenticatorTokenCredentialRepositoryEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("googleAuthenticatorTokenCredentialRepositoryEndpoint")
    private GoogleAuthenticatorTokenCredentialRepositoryEndpoint endpoint;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Test
    public void verifyOperation() {
        val acct = registry.create(UUID.randomUUID().toString());
        val toSave = OneTimeTokenAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        assertNotNull(endpoint.get(acct.getUsername()));
        assertFalse(endpoint.load().isEmpty());
        endpoint.delete(acct.getUsername());
        assertTrue(endpoint.get(acct.getUsername()).isEmpty());
        endpoint.deleteAll();
        assertTrue(endpoint.load().isEmpty());
    }
}
