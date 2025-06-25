package com.yubico.core;

import com.yubico.webauthn.data.*;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link WebSessionWebAuthnCache}.
 *
 * @author Jerome LELEU
 * @since 7.3.0
 */
@Tag("MFAProvider")
class WebSessionWebAuthnCacheTests {

    private static final ByteArray KEY = new ByteArray("key".getBytes(StandardCharsets.UTF_8));
    private static final ByteArray VALUE = new ByteArray("value".getBytes(StandardCharsets.UTF_8));

    private MockHttpServletRequest request;

    private WebAuthnCache<ByteArray> cache;

    @BeforeEach
    public void beforeEach() {
        request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        cache = new WebSessionWebAuthnCache<>("test", ByteArray.class);
    }

    @Test
    void verifyGet() {
        val value = cache.getIfPresent(request, KEY);
        assertNull(value);
    }

    @Test
    void verifyGetAndLoad() {
        val value = cache.get(request, KEY, v -> VALUE);
        assertEquals(VALUE, value);

        val newValue = cache.getIfPresent(request, KEY);
        assertEquals(VALUE, newValue);
    }

    @Test
    void verifyPutInvalidateGet() {
        cache.put(request, KEY, VALUE);
        val value = cache.getIfPresent(request, KEY);
        assertEquals(VALUE, value);

        cache.invalidate(request, KEY);

        val newValue = cache.getIfPresent(request, KEY);
        assertNull(newValue);
    }
}
