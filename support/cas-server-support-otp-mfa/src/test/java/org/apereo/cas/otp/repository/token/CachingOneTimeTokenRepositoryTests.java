package org.apereo.cas.otp.repository.token;

import module java.base;
import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachingOneTimeTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseOneTimeTokenRepositoryTests.SharedTestConfiguration.class)
@Getter
@Tag("MFA")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "repository", mode = ResourceAccessMode.READ_WRITE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CachingOneTimeTokenRepositoryTests extends BaseOneTimeTokenRepositoryTests {

    @Autowired
    @Qualifier(OneTimeTokenRepository.BEAN_NAME)
    private OneTimeTokenRepository repository;

    @Test
    @Order(1)
    void verifyTokenSave() {
        val casuser = UUID.randomUUID().toString();
        val token = new OneTimeToken(RandomUtils.nextInt(), casuser);
        repository.store(token);
        assertNull(repository.store(token));
        assertEquals(1, repository.count(casuser));
        repository.clean();
        assertTrue(repository.exists(casuser, token.getToken()));
        repository.remove(casuser);
        repository.remove(token.getToken());
        repository.remove(casuser, token.getToken());
        assertNull(repository.get(casuser, token.getToken()));
        assertEquals(0, repository.count());
    }

    @Test
    @Order(2)
    void verifyParallelOtpRequests() throws Exception {
        val otp = new OneTimeToken(RandomUtils.nextInt(100_000, 999_999), UUID.randomUUID().toString());
        try (val pool = Executors.newVirtualThreadPerTaskExecutor()) {
            val startGate = new CountDownLatch(1);
            val successes = new AtomicInteger();
            val attempt = (Callable<Void>) () -> {
                startGate.await();
                if (repository.store(otp) != null) {
                    successes.incrementAndGet();
                }
                return null;
            };

            val f1 = pool.submit(attempt);
            val f2 = pool.submit(attempt);
            startGate.countDown();
            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);
            pool.shutdownNow();
            assertEquals(1, successes.get(), "The same OTP was accepted twice where expected exactly 1 success.");
        }
    }

    @Test
    @Order(100)
    void verifyOperation() {
        val id = UUID.randomUUID().toString();
        val token = new OneTimeToken(RandomUtils.nextInt(), id);
        repository.store(token);
        repository.remove(token.getUserId(), token.getToken());
        assertFalse(repository.exists(token.getUserId(), token.getToken()));
        repository.removeAll();
        assertEquals(0, repository.count());
    }

}
