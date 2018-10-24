package org.apereo.cas.gauth.credential;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.SchedulingUtils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseOneTimeTokenCredentialRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
public abstract class BaseOneTimeTokenCredentialRepositoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    public static final String CASUSER = "casusergauth";
    public static final String PLAIN_SECRET = "plain_secret";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private IGoogleAuthenticator google;

    @Mock
    private CipherExecutor<String, String> cipherExecutor;

    private final Map<Pair<String, String>, OneTimeTokenAccount> accountHashMap = new LinkedHashMap<>();

    public OneTimeTokenAccount getAccount(final String testName, final String username) {
        return accountHashMap.computeIfAbsent(Pair.of(testName, username), pair -> getRegistry(pair.getLeft()).create(pair.getRight()));
    }

    @BeforeEach
    public void initialize() {
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @Test
    public void verifyCreate() {
        val acct = getAccount("verifyCreate", CASUSER);
        assertNotNull(acct);
    }

    @Test
    public void verifySaveAndUpdate() throws Exception {
        val acct = getAccount("verifySaveAndUpdate", CASUSER);
        val repo = getRegistry("verifySaveAndUpdate");
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());
        var s = repo.get(acct.getUsername());
        assertNotNull("Account not found", s);
        assertNotNull(s.getRegistrationDate());
        assertEquals(acct.getValidationCode(), s.getValidationCode());
        assertEquals(acct.getSecretKey(), s.getSecretKey());
        s.setSecretKey("newSecret");
        s.setValidationCode(999666);
        getRegistry("verifySaveAndUpdate").update(s);
        s = getRegistry("verifySaveAndUpdate").get(CASUSER);
        assertEquals(999666, s.getValidationCode());
        assertEquals("newSecret", s.getSecretKey());
    }

    @Test
    public void verifyGet() throws Exception {
        val repo = getRegistry("verifyGet");
        val acct = repo.get(CASUSER);
        assertNull(acct);
        val acct2 = getAccount("verifyGet", CASUSER);
        repo.save(acct2.getUsername(), acct2.getSecretKey(), acct2.getValidationCode(), acct2.getScratchCodes());
        val acct3 = repo.get(CASUSER);
        assertNotNull("Account not found", acct3);
        assertEquals(acct2.getUsername(), acct3.getUsername());
        assertEquals(acct2.getValidationCode(), acct3.getValidationCode());
        assertEquals(acct2.getSecretKey(), acct3.getSecretKey());
        assertEquals(acct2.getScratchCodes(), acct3.getScratchCodes());
    }

    @Test
    public void verifyGetWithDecodedSecret() throws Exception {
        // given
        when(cipherExecutor.encode(PLAIN_SECRET)).thenReturn("abc321");
        when(cipherExecutor.decode("abc321")).thenReturn(PLAIN_SECRET);
        val repo = getRegistry("verifyGetWithDecodedSecret");
        var acct = getAccount("verifyGetWithDecodedSecret", CASUSER);
        acct.setSecretKey(PLAIN_SECRET);
        repo.save(acct.getUsername(), acct.getSecretKey(), acct.getValidationCode(), acct.getScratchCodes());

        // when
        acct = repo.get(CASUSER);

        // then
        assertEquals(PLAIN_SECRET, acct.getSecretKey());
    }

    public OneTimeTokenCredentialRepository getRegistry(final String testName) {
        return getRegistry();
    };

    public abstract OneTimeTokenCredentialRepository getRegistry();

    @TestConfiguration
    public static class BaseTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
}
