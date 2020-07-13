package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.util.crypto.CipherExecutor;

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
@Tag("Simple")
public class EncryptedCookieValueManagerTests {

    @Test
    public void verifyNoValue() {
        val mgr = new EncryptedCookieValueManager(mock(CipherExecutor.class));
        assertNull(mgr.obtainCookieValue("something", new MockHttpServletRequest()));
    }

    @Test
    public void verifyEmptyValue() {
        val cipher = mock(CipherExecutor.class);
        when(cipher.decode(anyString(), any())).thenReturn(StringUtils.EMPTY);
        val mgr = new EncryptedCookieValueManager(cipher);
        assertNull(mgr.obtainCookieValue("something", new MockHttpServletRequest()));
    }
}
