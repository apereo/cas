package org.apereo.cas.web.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConcurrentThrottledSubmissionsStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("AuthenticationThrottling")
@SpringBootTest(classes = BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ConcurrentThrottledSubmissionsStoreTests {
    @Autowired
    @Qualifier(ThrottledSubmissionsStore.BEAN_NAME)
    private ThrottledSubmissionsStore<ThrottledSubmission> throttleSubmissionStore;

    @Test
    void verifyOperation() throws Throwable {
        val key = UUID.randomUUID().toString();
        throttleSubmissionStore.put(ThrottledSubmission.builder().key(key).build());
        assertNotNull(throttleSubmissionStore.get(key));
        assertEquals(1, throttleSubmissionStore.entries().count());
        throttleSubmissionStore.removeIf(entry -> entry.getKey().equals(key));
        throttleSubmissionStore.remove(key);
        assertEquals(0, throttleSubmissionStore.entries().count());
    }

    @Test
    void verifyExpiredSubmissions() throws Throwable {
        val submission = ThrottledSubmission
            .builder()
            .key(UUID.randomUUID().toString())
            .expiration(ZonedDateTime.now(Clock.systemUTC()).plusSeconds(2))
            .build();
        throttleSubmissionStore.put(submission);
        throttleSubmissionStore.release(0.5);
        assertNotNull(throttleSubmissionStore.get(submission.getKey()));

        val expiredSubmission = ThrottledSubmission
            .builder()
            .key(UUID.randomUUID().toString())
            .expiration(ZonedDateTime.now(Clock.systemUTC()).minusSeconds(2))
            .build();
        throttleSubmissionStore.put(expiredSubmission);
        throttleSubmissionStore.release(0.01);
        assertNull(throttleSubmissionStore.get(expiredSubmission.getKey()));
    }

    @Test
    void verifyExpirationWindowDuringRelease() throws Throwable {
        val submission = ThrottledSubmission
            .builder()
            .key(UUID.randomUUID().toString())
            .expiration(ZonedDateTime.now(Clock.systemUTC()).plusSeconds(25))
            .build();
        throttleSubmissionStore.put(submission);
        throttleSubmissionStore.release(0.5);
        assertNotNull(throttleSubmissionStore.get(submission.getKey()));
    }
}
