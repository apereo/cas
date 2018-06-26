package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Slf4j
public class InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests {
    private IGoogleAuthenticator google;

    @Before
    public void initialize() {
        final var bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @Test
    public void verifyCreate() {
        final var repo =
            new InMemoryGoogleAuthenticatorTokenCredentialRepository(CipherExecutor.noOpOfStringToString(), google);
        final var acct = repo.create("casuser");
        assertNotNull(acct);
    }

    @Test
    public void verifyGet() {
        final var repo =
            new InMemoryGoogleAuthenticatorTokenCredentialRepository(CipherExecutor.noOpOfStringToString(), google);
        var acct = repo.get("casuser");
        assertNull(acct);
        acct = repo.create("casuser");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
        acct = repo.get("casuser");
        assertNotNull(acct);
    }
}
