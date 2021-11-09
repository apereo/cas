package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.config.GoogleAuthenticatorRedisConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    GoogleAuthenticatorRedisConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.mfa.gauth.redis.host=localhost",
    "cas.authn.mfa.gauth.redis.port=6379"
})
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableScheduling
@Tag("Redis")
@Getter
@EnabledIfPortOpen(port = 6379)
public class RedisGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @BeforeEach
    public void cleanUp() {
        registry.deleteAll();
    }

    @Test
    public void verifySave() {
        val id = UUID.randomUUID().toString();
        assertNull(registry.get(654321));
        assertNull(registry.get(id, 654321));

        var toSave = OneTimeTokenAccount.builder()
            .username(id)
            .secretKey("secret")
            .validationCode(143211)
            .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        
        val s = registry.get(id).iterator().next();
        assertEquals("secret", s.getSecretKey());
        val c = registry.load();
        assertFalse(c.isEmpty());
    }

    @Test
    public void verifyDelete() {
        val id = UUID.randomUUID().toString();
        val toSave = OneTimeTokenAccount.builder()
            .username(id)
            .secretKey("secret")
            .validationCode(143211)
            .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        registry.delete(id);
        assertEquals(0, registry.count());
    }

    @Test
    public void verifySaveAndUpdate() {
        val id = UUID.randomUUID().toString();
        val toSave = OneTimeTokenAccount.builder()
            .username(id)
            .secretKey("secret")
            .validationCode(222222)
            .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        val s = registry.get(id).iterator().next();
        assertNotNull(s.getRegistrationDate());
        assertEquals(222222, s.getValidationCode());
        s.setSecretKey("newSecret");
        s.setValidationCode(999666);
        registry.update(s);
        val s2 = registry.get(id).iterator().next();
        assertEquals(999666, s2.getValidationCode());
        assertEquals("newSecret", s2.getSecretKey());
    }
}
