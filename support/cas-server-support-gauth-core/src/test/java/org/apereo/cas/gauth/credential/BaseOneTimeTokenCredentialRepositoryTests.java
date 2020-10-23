package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseOneTimeTokenCredentialRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Tag("MFA")
public abstract class BaseOneTimeTokenCredentialRepositoryTests {
    public static final String PLAIN_SECRET = "plain_secret";

    private final Map<Pair<String, String>, OneTimeTokenAccount> accountHashMap = new LinkedHashMap<>();

    private IGoogleAuthenticator google;

    @Mock
    private CipherExecutor<String, String> cipherExecutor;

    public OneTimeTokenAccount getAccount(final String testName, final String username) {
        return accountHashMap.computeIfAbsent(Pair.of(testName, username), pair -> getRegistry(pair.getLeft()).create(pair.getRight()));
    }

    @BeforeEach
    public void initialize() {
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @AfterEach
    public void afterEach() {
        val repo = getRegistry("afterEach");
        repo.deleteAll();
    }

    @Test
    public void verifyCreate() {
        val casuser = getUsernameUnderTest();
        val acct = getAccount("verifyCreate", casuser);
        assertNotNull(acct);
        val repo = getRegistry("verifyCreate");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
        assertEquals(1, repo.count());
        repo.delete(acct.getUsername());
        assertTrue(repo.load().isEmpty());
        assertEquals(0, repo.count());
    }

    @Test
    public void verifySaveAndUpdate() {
        val casuser = getUsernameUnderTest();
        val acct = getAccount("verifySaveAndUpdate", casuser);
        val repo = getRegistry("verifySaveAndUpdate");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
        var s = repo.get(acct.getUsername());
        assertNotNull(s, "Account not found");
        assertNotNull(s.getRegistrationDate());
        assertEquals(acct.getValidationCode(), s.getValidationCode());
        assertEquals(acct.getSecretKey(), s.getSecretKey());
        s.setSecretKey("newSecret");
        s.setValidationCode(999666);
        getRegistry("verifySaveAndUpdate").update(s);
        s = getRegistry("verifySaveAndUpdate").get(casuser);
        assertEquals(999666, s.getValidationCode());
        assertEquals("newSecret", s.getSecretKey());
    }

    @Test
    public void verifyGet() {
        val casuser = getUsernameUnderTest();
        val repo = getRegistry("verifyGet");
        val acct = repo.get(casuser);
        assertNull(acct);
        val acct2 = getAccount("verifyGet", casuser);
        repo.save(acct2.getUsername(), acct2.getSecretKey(), acct2.getValidationCode(), acct2.getScratchCodes());
        val acct3 = repo.get(casuser);
        assertNotNull(acct3, "Account not found");
        assertEquals(acct2.getUsername(), acct3.getUsername());
        assertEquals(acct2.getValidationCode(), acct3.getValidationCode());
        assertEquals(acct2.getSecretKey(), acct3.getSecretKey());
        assertEquals(acct2.getScratchCodes(), acct3.getScratchCodes());
    }

    @Test
    public void verifyGetWithDecodedSecret() {
        val casuser = getUsernameUnderTest();
        when(cipherExecutor.encode(PLAIN_SECRET)).thenReturn("abc321");
        when(cipherExecutor.decode("abc321")).thenReturn(PLAIN_SECRET);
        val repo = getRegistry("verifyGetWithDecodedSecret");
        var acct = getAccount("verifyGetWithDecodedSecret", casuser);
        acct.setSecretKey(PLAIN_SECRET);
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());

        acct = repo.get(casuser);
        assertEquals(PLAIN_SECRET, acct.getSecretKey());
    }

    public OneTimeTokenCredentialRepository getRegistry(final String testName) {
        return getRegistry();
    }

    public abstract OneTimeTokenCredentialRepository getRegistry();

    @TestConfiguration
    @Lazy(false)
    public static class BaseTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }

    protected String getUsernameUnderTest() {
        return UUID.randomUUID().toString();
    }
}
