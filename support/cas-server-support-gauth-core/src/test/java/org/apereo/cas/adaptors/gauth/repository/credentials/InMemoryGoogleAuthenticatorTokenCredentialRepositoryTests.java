package org.apereo.cas.adaptors.gauth.repository.credentials;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreUtilConfiguration;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class InMemoryGoogleAuthenticatorTokenCredentialRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private IGoogleAuthenticator google;
    @Mock
    private CipherExecutor<String, String> cipherExecutor;

    @Before
    public void initialize() {
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @Test
    public void verifyCreate() {
        val repo = new InMemoryGoogleAuthenticatorTokenCredentialRepository(CipherExecutor.noOpOfStringToString(), google);
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

    @Test
    public void verifyGetWithDecodedSecret() {
        // given
        when(cipherExecutor.encode("plain_secret")).thenReturn("abc321");
        when(cipherExecutor.decode("abc321")).thenReturn("plain_secret");
        val repo =
            new InMemoryGoogleAuthenticatorTokenCredentialRepository(cipherExecutor, google);
        var acct = repo.create("casuser");
        acct.setSecretKey("plain_secret");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());

        // when
        acct = repo.get("casuser");

        // then
        assertEquals("plain_secret", acct.getSecretKey());
    }
}
