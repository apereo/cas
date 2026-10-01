package org.apereo.cas.util.crypto;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamSource;
import java.security.cert.X509Certificate;
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

    @Test
    void verifyChainWithoutTrustAnchor() {
        val root = mockCertificate("CN=Root", "CN=Root");
        val intermediate = mockCertificate("CN=Intermediate", "CN=Root");
        val leaf = mockCertificate("CN=Leaf", "CN=Intermediate");
        assertEquals(List.of(leaf, intermediate), CertUtils.withoutTrustAnchor(List.of(leaf, intermediate, root)));
        assertEquals(List.of(leaf, intermediate), CertUtils.withoutTrustAnchor(List.of(leaf, intermediate)));
        assertEquals(List.of(root), CertUtils.withoutTrustAnchor(List.of(root)));
        assertTrue(CertUtils.withoutTrustAnchor(List.of()).isEmpty());
        assertTrue(CertUtils.withoutTrustAnchor(null).isEmpty());
    }

    private static X509Certificate mockCertificate(final String subject, final String issuer) {
        val certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal(subject));
        when(certificate.getIssuerX500Principal()).thenReturn(new X500Principal(issuer));
        return certificate;
    }
}
