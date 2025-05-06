package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutorResolver;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link EncryptedCookieValueManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Cookie")
class EncryptedCookieValueManagerTests {

    @Test
    void verifyNoValue() {
        val mgr = new EncryptedCookieValueManager(CipherExecutorResolver.withCipherExecutor(mock(CipherExecutor.class)),
            mock(TenantExtractor.class), DefaultCookieSameSitePolicy.INSTANCE);
        assertNull(mgr.obtainCookieValue("something", new MockHttpServletRequest()));
    }

    @Test
    void verifyEmptyValue() {
        val cipher = mock(CipherExecutor.class);
        when(cipher.decode(anyString(), any())).thenReturn(StringUtils.EMPTY);
        val mgr = new EncryptedCookieValueManager(CipherExecutorResolver.withCipherExecutor(cipher),
            mock(TenantExtractor.class), DefaultCookieSameSitePolicy.INSTANCE);
        assertNull(mgr.obtainCookieValue("something", new MockHttpServletRequest()));
    }
}
