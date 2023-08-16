package org.apereo.cas.throttle;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.ThrottledSubmission;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConcurrentThrottledSubmissionsStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("AuthenticationThrottling")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ConcurrentThrottledSubmissionsStoreTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperation() throws Throwable {
        val store = new ConcurrentThrottledSubmissionsStore(casProperties);
        val key = UUID.randomUUID().toString();
        store.put(ThrottledSubmission.builder().key(key).build());
        assertNotNull(store.get(key));
        assertEquals(1, store.entries().count());
        store.removeIf(entry -> entry.getKey().equals(key));
        store.remove(key);
        assertEquals(0, store.entries().count());
    }
}
