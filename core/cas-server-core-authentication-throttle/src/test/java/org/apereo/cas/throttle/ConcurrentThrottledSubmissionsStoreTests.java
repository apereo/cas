package org.apereo.cas.throttle;

import org.apereo.cas.web.support.ThrottledSubmission;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConcurrentThrottledSubmissionsStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("AuthenticationThrottling")
public class ConcurrentThrottledSubmissionsStoreTests {
    @Test
    public void verifyOperation() {
        val store = new ConcurrentThrottledSubmissionsStore();
        val key = UUID.randomUUID().toString();
        store.put(ThrottledSubmission.builder().key(key).build());
        assertNotNull(store.get(key));
        assertEquals(1, store.entries().count());
        store.removeIf(entry -> entry.getKey().equals(key));
        assertEquals(0, store.entries().count());
    }
}
