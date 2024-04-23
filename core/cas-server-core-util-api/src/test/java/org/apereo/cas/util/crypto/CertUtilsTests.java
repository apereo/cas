package org.apereo.cas.util.crypto;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CertUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
class CertUtilsTests {
    @Test
    void verifyOperation() throws Throwable {
        val source = mock(InputStreamSource.class);
        when(source.getInputStream()).thenThrow(new RuntimeException());
        assertThrows(IllegalArgumentException.class, () -> CertUtils.readCertificate(source));
    }
}
