package org.apereo.cas.adaptors.x509.authentication;

import module java.base;
import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RevokedCertificateExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("X509")
class RevokedCertificateExceptionTests {
    @Test
    void verifyReason() {
        val entry = mock(X509CRLEntry.class);
        when(entry.hasExtensions()).thenReturn(Boolean.TRUE);
        when(entry.getSerialNumber()).thenReturn(BigInteger.ONE);
        when(entry.getRevocationDate()).thenReturn(new Date());
        when(entry.getExtensionValue(anyString())).thenReturn("3".getBytes(StandardCharsets.UTF_8));
        val results = new RevokedCertificateException(entry);
        assertNotNull(results.getReason());
        assertNotNull(results.getMessage());
    }
}
