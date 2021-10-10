package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

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
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.util.LinkedHashMap;
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
        assertNotNull(repo.get(toSave.getUsername(), toSave.getId()));
        assertEquals(1, repo.count());
        assertEquals(1, repo.count(toSave.getUsername()));
        repo.delete(acct.getUsername());
        assertTrue(repo.load().isEmpty());
        assertEquals(0, repo.count());
        assertEquals(0, repo.count(toSave.getUsername()));
    }

    @Test
    public void verifySaveAndUpdate() {
        val casuser = getUsernameUnderTest();
        val acct = getAccount("verifySaveAndUpdate", casuser);
        val repo = getRegistry("verifySaveAndUpdate");
        var toSave = OneTimeTokenAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(casuser)
            .build();
        repo.save(toSave);
        var s = repo.get(acct.getUsername()).iterator().next();
        assertNotNull(s, "Account not found");
        assertNotNull(s.getRegistrationDate());
        assertEquals(acct.getValidationCode(), s.getValidationCode());
        assertEquals(acct.getSecretKey(), s.getSecretKey());
        s.setSecretKey("newSecret");
        s.setValidationCode(999666);
        repo.update(s);
        val accts = repo.get(casuser);
        s = accts.iterator().next();
        assertEquals(999666, s.getValidationCode());
        assertEquals("newSecret", s.getSecretKey());

        repo.delete(s.getId());
        assertNull(repo.get(s.getId()));
    }

    @Test
    public void verifyGet() {
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
        assertEquals(acct2.getScratchCodes().stream().sorted().collect(Collectors.toList()),
            acct3.getScratchCodes().stream().sorted().collect(Collectors.toList()));
        repo.delete(acct3.getId());
    }

    @Test
    public void verifyCaseSensitivity() {
        val casuser = getUsernameUnderTest().toLowerCase();
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
        assertNotNull(repo.get(toSave.getUsername().toUpperCase(), toSave.getId()));
        assertEquals(1, repo.count());
        assertEquals(1, repo.count(toSave.getUsername().toUpperCase()));
        repo.delete(acct.getUsername().toUpperCase());
        assertTrue(repo.load().isEmpty());
        assertEquals(0, repo.count());
        assertEquals(0, repo.count(toSave.getUsername().toUpperCase()));
    }

    @Test
    public void verifyGetWithDecodedSecret() {
        val casuser = getUsernameUnderTest();
        when(cipherExecutor.encode(PLAIN_SECRET)).thenReturn("abc321");
        when(cipherExecutor.decode("abc321")).thenReturn(PLAIN_SECRET);
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

    protected String getUsernameUnderTest() {
        return UUID.randomUUID().toString();
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreUtilConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreWebConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
