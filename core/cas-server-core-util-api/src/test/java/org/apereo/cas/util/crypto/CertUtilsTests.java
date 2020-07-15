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
public class CertUtilsTests {
    @Test
    public void verifyOperation() throws Exception {
        val source = mock(InputStreamSource.class);
        when(source.getInputStream()).thenThrow(new RuntimeException());
        assertThrows(IllegalArgumentException.class, () -> CertUtils.readCertificate(source));
    }
}
