package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.config.CasGoogleAuthenticatorRedisAutoConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasGoogleAuthenticatorRedisAutoConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.mfa.gauth.redis.host=localhost",
    "cas.authn.mfa.gauth.redis.port=6379"
})
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Tag("Redis")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 6379)
@Slf4j
class RedisGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {
    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;
    
    @Test
    void verifySave() {
        val username = UUID.randomUUID().toString();
        assertNull(registry.get(654321));
        assertNull(registry.get(username, 654321));
        val validationCode = RandomUtils.nextInt(1, 999999);
        val toSave = OneTimeTokenAccount.builder()
            .username(username)
            .secretKey("secret")
            .validationCode(validationCode)
            .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);

        val account = registry.get(username).iterator().next();
        assertEquals("secret", account.getSecretKey());
        assertEquals(validationCode, account.getValidationCode());
        val accounts = registry.load();
        assertFalse(accounts.isEmpty());
    }

    @Test
    void verifyDelete() {
        val username = UUID.randomUUID().toString();
        val toSave = OneTimeTokenAccount.builder()
            .username(username)
            .secretKey("secret")
            .validationCode(143211)
            .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        registry.delete(username);
        assertEquals(0, registry.count());
    }

    @Override
    @RetryingTest(2)
    void verifySaveAndUpdate() {
        val username = UUID.randomUUID().toString();
        var validationCode = RandomUtils.nextInt(1, 999999);
        val toSave = OneTimeTokenAccount.builder()
            .username(username)
            .secretKey("secret")
            .validationCode(validationCode)
            .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        val tokenAccount = registry.get(username).iterator().next();
        assertNotNull(tokenAccount.getRegistrationDate());
        assertEquals(validationCode, tokenAccount.getValidationCode());
        tokenAccount.setSecretKey("newSecret");

        val validationCode2 = RandomUtils.nextInt(1, 999999);
        tokenAccount.setValidationCode(validationCode2);
        registry.update(tokenAccount);

        await().untilAsserted(() -> {
            val s2 = registry.get(username).iterator().next();
            assertEquals(validationCode2, s2.getValidationCode());
            assertEquals("newSecret", s2.getSecretKey());
        });
    }

    @Test
    void verifyLargeDataset() {
        val allAccounts = Stream.generate(
                () -> {
                    val username = UUID.randomUUID().toString();
                    var validationCode = RandomUtils.nextInt(1, 999999);
                    return OneTimeTokenAccount.builder()
                        .username(username)
                        .secretKey("secret")
                        .validationCode(validationCode)
                        .scratchCodes(CollectionUtils.wrapList(1, 2, 3, 4, 5, 6))
                        .name(UUID.randomUUID().toString())
                        .build();
                })
            .limit(1000);
        executedTimedOperation("Adding accounts", __ -> allAccounts.forEach(registry::save));
        executedTimedOperation("Getting accounts",
            Unchecked.consumer(__ -> {
                val accounts = registry.load();
                assertFalse(accounts.isEmpty());
            }));

        val accountsStream = executedTimedOperation("Getting accounts in bulk",
            Unchecked.supplier(() -> registry.load()));
        executedTimedOperation("Getting accounts individually",
            Unchecked.consumer(__ -> accountsStream.forEach(acct -> assertNotNull(registry.get(acct.getId())))));
        executedTimedOperation("Getting accounts individually for users",
            Unchecked.consumer(__ -> accountsStream.forEach(acct -> assertNotNull(registry.get(acct.getUsername())))));
    }

    private static <T> T executedTimedOperation(final String name, final Supplier<T> operation) {
        val stopwatch = new StopWatch();
        stopwatch.start();
        val result = operation.get();
        stopwatch.stop();
        val time = stopwatch.getTime(TimeUnit.MILLISECONDS);
        LOGGER.info("[{}]: [{}]ms", name, time);
        assertTrue(time <= 8000);
        return result;
    }

    private static void executedTimedOperation(final String name, final Consumer operation) {
        val stopwatch = new StopWatch();
        stopwatch.start();
        operation.accept(null);
        stopwatch.stop();
        val time = stopwatch.getTime(TimeUnit.MILLISECONDS);
        LOGGER.info("[{}]: [{}]ms", name, time);
        assertTrue(time <= 8000);
    }
}
