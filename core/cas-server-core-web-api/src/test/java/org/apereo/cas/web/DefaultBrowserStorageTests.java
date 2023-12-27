package org.apereo.cas.web;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultBrowserStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Web")
public class DefaultBrowserStorageTests {
    @Test
    void verifyOperation() throws Exception {
        val storage = DefaultBrowserStorage.builder()
            .context("Context")
            .build()
            .setPayloadJson(Map.of("Hello", "World"));
        assertNotNull(storage.getPayloadJson(Map.class));
    }
}
