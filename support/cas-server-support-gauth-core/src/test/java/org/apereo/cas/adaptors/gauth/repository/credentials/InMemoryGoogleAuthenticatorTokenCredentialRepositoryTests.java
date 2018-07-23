package org.apereo.cas.adaptors.gauth.repository.credentials;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreUtilConfiguration;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
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
public class InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests {
    private IGoogleAuthenticator google;

    @Before
    public void initialize() {
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @Test
    public void verifyCreate() {
        val repo =
            new InMemoryGoogleAuthenticatorTokenCredentialRepository(CipherExecutor.noOpOfStringToString(), google);
        val acct = repo.create("casuser");
        assertNotNull(acct);
    }

    @Test
    public void verifyGet() {
        val repo =
            new InMemoryGoogleAuthenticatorTokenCredentialRepository(CipherExecutor.noOpOfStringToString(), google);
        var acct = repo.get("casuser");
        assertNull(acct);
        acct = repo.create("casuser");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
        acct = repo.get("casuser");
        assertNotNull(acct);
    }
}
