package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseOneTimeTokenCredentialRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
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
    void initialize() {
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        this.google = new GoogleAuthenticator(bldr.build());
    }

    @AfterEach
    public void afterEach() {
        val repo = getRegistry("afterEach");
        repo.deleteAll();
    }

    @Test
    void verifyCreate() throws Throwable {
        val casuser = getUsernameUnderTest();
        val acct = getAccount("verifyCreate", casuser);
        acct.setProperties(CollectionUtils.wrapList("prop1", "prop2"));
        assertNotNull(acct);
        val repo = getRegistry("verifyCreate");

        val toSave = OneTimeTokenAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(casuser)
            .properties(acct.getProperties())
            .build();
        val stored = repo.save(toSave);
        assertNotNull(repo.get(stored.getId()));
        assertNotNull(repo.get(toSave.getUsername(), stored.getId()));
        assertEquals(1, repo.count());
        assertEquals(1, repo.count(stored.getUsername()));
        repo.delete(acct.getUsername());
        assertTrue(repo.load().isEmpty());
        assertEquals(0, repo.count());
        assertEquals(0, repo.count(stored.getUsername()));
    }

    @Test
    void verifySaveAndUpdate() throws Throwable {
        val casuser = getUsernameUnderTest();
        val acct = getAccount("verifySaveAndUpdate", casuser);
        acct.setProperties(CollectionUtils.wrapList("prop1", "prop2"));
        val repo = getRegistry("verifySaveAndUpdate");
        var toSave = OneTimeTokenAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(casuser)
            .build();
        repo.save(toSave);
        var account = repo.get(acct.getUsername()).iterator().next();
        assertNotNull(account, "Account not found");
        assertNotNull(account.getRegistrationDate());
        assertEquals(acct.getValidationCode(), account.getValidationCode());
        assertEquals(acct.getSecretKey(), account.getSecretKey());
        account.setSecretKey("newSecret");
        account.setValidationCode(999666);
        repo.update(account);
        val accts = repo.get(casuser);
        account = accts.iterator().next();
        assertEquals(999666, account.getValidationCode());
        assertEquals("newSecret", account.getSecretKey());

        repo.delete(account.getId());
        assertNull(repo.get(account.getId()));
    }

    @Test
    void verifyGet() throws Throwable {
        val casuser = getUsernameUnderTest();
        val repo = getRegistry("verifyGet");
        val acct = repo.get(casuser);
        assertTrue(acct.isEmpty());
        val acct2 = getAccount("verifyGet", casuser);
        val toSave = OneTimeTokenAccount.builder()
            .username(acct2.getUsername())
            .secretKey(acct2.getSecretKey())
            .validationCode(acct2.getValidationCode())
            .scratchCodes(acct2.getScratchCodes())
            .name(casuser)
            .build();
        repo.save(toSave);
        val acct3 = repo.get(casuser).iterator().next();
        assertNotNull(acct3, "Account not found");
        assertEquals(acct2.getUsername(), acct3.getUsername());
        assertEquals(acct2.getValidationCode(), acct3.getValidationCode());
        assertEquals(acct2.getSecretKey(), acct3.getSecretKey());
        assertEquals(acct2.getScratchCodes().stream().sorted().map(Number::intValue).collect(Collectors.toList()),
            acct3.getScratchCodes().stream().sorted().map(Number::intValue).collect(Collectors.toList()));
        repo.delete(acct3.getId());
    }

    @Test
    void verifyCaseSensitivity() throws Throwable {
        val casuser = getUsernameUnderTest().toLowerCase(Locale.ENGLISH);
        val acct = getAccount("verifyCaseSensitivity", casuser);
        assertNotNull(acct);
        val repo = getRegistry("verifyCaseSensitivity");

        var toSave = OneTimeTokenAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(casuser)
            .build();
        toSave = repo.save(toSave);
        assertNotNull(toSave);
        assertNotNull(repo.get(toSave.getId()));
        assertNotNull(repo.get(toSave.getUsername().toUpperCase(Locale.ENGLISH), toSave.getId()));
        assertEquals(1, repo.count());
        assertEquals(1, repo.count(toSave.getUsername().toUpperCase(Locale.ENGLISH)));
        repo.delete(acct.getUsername().toUpperCase(Locale.ENGLISH));
        assertTrue(repo.load().isEmpty());
        assertEquals(0, repo.count());
        assertEquals(0, repo.count(toSave.getUsername().toUpperCase(Locale.ENGLISH)));
    }

    @Test
    void verifyGetWithDecodedSecret() throws Throwable {
        val casuser = getUsernameUnderTest();
        lenient().when(cipherExecutor.encode(PLAIN_SECRET)).thenReturn("abc321");
        lenient().when(cipherExecutor.decode("abc321")).thenReturn(PLAIN_SECRET);
        val repo = getRegistry("verifyGetWithDecodedSecret");
        var acct = getAccount("verifyGetWithDecodedSecret", casuser);
        acct.setSecretKey(PLAIN_SECRET);
        val toSave = OneTimeTokenAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(casuser)
            .build();
        repo.save(toSave);

        acct = repo.get(casuser).iterator().next();
        assertEquals(PLAIN_SECRET, acct.getSecretKey());
    }

    public OneTimeTokenCredentialRepository getRegistry(final String testName) {
        return getRegistry();
    }

    public abstract OneTimeTokenCredentialRepository getRegistry();

    protected String getUsernameUnderTest() throws Exception {
        return UUID.randomUUID().toString();
    }
    
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasGoogleAuthenticatorAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }
}
