package com.yubico.core;

import com.yubico.webauthn.data.*;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

    private WebAuthnCache<ByteArray> cache;

    @BeforeEach
    public void beforeEach() {
        val requestAttributes = mock(ServletRequestAttributes.class);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        val request = new MockHttpServletRequest();
        when(requestAttributes.getRequest()).thenReturn(request);
        request.setSession(new MockHttpSession());

        cache = new WebSessionWebAuthnCache<>("test", ByteArray.class);
    }

    @Test
    void verifyGet() {
        val value = cache.getIfPresent(KEY);
        assertNull(value);
    }

    @Test
    void verifyGetAndLoad() {
        val value = cache.get(KEY, v -> VALUE);
        assertEquals(VALUE, value);

        val newValue = cache.getIfPresent(KEY);
        assertEquals(VALUE, newValue);
    }

    @Test
    void verifyPutInvalidateGet() {
        cache.put(KEY, VALUE);
        val value = cache.getIfPresent(KEY);
        assertEquals(VALUE, value);

        cache.invalidate(KEY);

        val newValue = cache.getIfPresent(KEY);
        assertNull(newValue);
    }
}
