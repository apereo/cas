package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.config.CasGoogleAuthenticatorJpaAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link JpaGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasGoogleAuthenticatorJpaAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.jdbc.show-sql=true",
        "cas.authn.mfa.gauth.core.scratch-codes.encryption.key=12345678901234567890123456789012",
        "cas.authn.mfa.gauth.crypto.enabled=true",
        "cas.authn.mfa.gauth.jpa.url=jdbc:hsqldb:mem:gauth-credentials;hsqldb.tx=mvcc"
    })
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Getter
@Tag("JDBCMFA")
@ExtendWith(CasTestExtension.class)
class JpaGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired(required = false)
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;

    @Test
    void verifyCreateUniqueNames() {
        var acct1 = getAccount("verifyCreateUniqueNames", UUID.randomUUID().toString());
        assertNotNull(acct1);
        val repo = getRegistry("verifyCreate");
        acct1 = repo.save(acct1);
        assertNotNull(acct1);

        var acct2 = getAccount("verifyCreateUniqueNames", UUID.randomUUID().toString());
        acct2.setName(acct1.getName());
        acct2 = repo.save(acct2);
        assertNotNull(acct2);

        acct2.setName("NewAccount");
        acct2 = repo.update(acct2);
        assertNotNull(acct2);

        acct1 = repo.save(acct1);
        assertNotNull(acct1);
    }

    @Test
    void verifyUpdateOfFetchedAccountKeepsSecretsAndProperties() {
        val username = UUID.randomUUID().toString();
        val repo = getRegistry("verifyUpdateOfFetchedAccountKeepsSecretsAndProperties");
        val account = repo.create(username);
        val secret = account.getSecretKey();
        val scratchCodes = account.getScratchCodes().stream().map(Number::intValue).sorted().toList();
        val id = repo.save(account).getId();

        assertEquals(secret, repo.get(id).getSecretKey());
        assertNull(repo.get(UUID.randomUUID().toString(), id));

        val owned = repo.get(username, id);
        assertEquals(secret, owned.getSecretKey());
        owned.getProperties().add("verified");
        repo.update(owned);

        val updated = repo.get(username, id);
        assertEquals(secret, updated.getSecretKey());
        assertEquals(scratchCodes, updated.getScratchCodes().stream().map(Number::intValue).sorted().toList());
        assertEquals(List.of("verified"), updated.getProperties());
    }

    @Test
    void verifyFormattedIdMatchesPersistedId() {
        val username = UUID.randomUUID().toString();
        val repo = getRegistry("verifyFormattedIdMatchesPersistedId");
        repo.save(getAccount("verifyFormattedIdMatchesPersistedId", username));
        val accounts = repo.get(username);
        assertEquals(1, accounts.size());
        val account = accounts.iterator().next();
        assertTrue(account.getId() > 0);
        assertEquals(String.valueOf(account.getId()), account.getFormattedId());
    }

    @Test
    void verifySaveWithUnknownIdAndDeleteById() {
        val username = UUID.randomUUID().toString();
        val repo = getRegistry("verifySaveWithUnknownIdAndDeleteById");
        val imported = repo.create(username).assignIdIfNecessary();
        imported.setProperties(new ArrayList<>(List.of("verified")));
        val saved = repo.save(imported);
        assertEquals(List.of("verified"), repo.get(username, saved.getId()).getProperties());

        repo.delete(saved.getId());
        assertNull(repo.get(saved.getId()));
        assertEquals(0, repo.count(username));
    }

    @Test
    void verifyDeleteByUsernameKeepsOtherUsers() {
        val username = UUID.randomUUID().toString();
        val otherUsername = UUID.randomUUID().toString();
        val repo = getRegistry("verifyDeleteByUsernameKeepsOtherUsers");
        for (var i = 0; i < 2; i++) {
            val account = repo.create(username);
            account.setProperties(new ArrayList<>(List.of("verified")));
            repo.save(account);
        }
        val other = repo.create(otherUsername);
        other.setProperties(new ArrayList<>(List.of("kept")));
        val otherScratchCodes = other.getScratchCodes().stream().map(Number::intValue).sorted().toList();
        val otherId = repo.save(other).getId();
        assertEquals(2, repo.count(username));

        repo.delete(username);
        assertEquals(0, repo.count(username));
        assertTrue(repo.get(username).isEmpty());
        val kept = repo.get(otherUsername, otherId);
        assertEquals(List.of("kept"), kept.getProperties());
        assertEquals(otherScratchCodes, kept.getScratchCodes().stream().map(Number::intValue).sorted().toList());
    }
}
