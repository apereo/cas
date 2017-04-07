package org.apereo.cas.adaptors.x509.authentication.principal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.junit.Test;

import java.math.BigInteger;
import java.security.cert.X509Certificate;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0.6
 *
 */
public class X509SerialNumberPrincipalResolverTests extends AbstractX509CertificateTests {

    private final X509SerialNumberPrincipalResolver resolver = new X509SerialNumberPrincipalResolver();

    @Test
    public void verifyResolvePrincipalInternal() {
        final X509CertificateCredential c = new X509CertificateCredential(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);

        assertEquals(VALID_CERTIFICATE.getSerialNumber().toString(),
                this.resolver.resolve(c, CoreAuthenticationTestUtils.getPrincipal(),
                        new SimpleTestUsernamePasswordAuthenticationHandler()).getId());
    }

    @Test
    public void verifySupport() {
        final X509CertificateCredential c = new X509CertificateCredential(new X509Certificate[] {VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(c));
    }

    @Test
    public void verifySupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyHexPrincipalOdd() {
        final X509SerialNumberPrincipalResolver r = new X509SerialNumberPrincipalResolver(16, true);
        final X509Certificate mockCert = mock(X509Certificate.class);
        when(mockCert.getSerialNumber()).thenReturn(BigInteger.valueOf(300L));

        final String principal = r.resolvePrincipalInternal(mockCert);
        assertEquals("012c", principal);
    }

    @Test
    public void verifyHexPrincipalOddFalse() {
        final X509SerialNumberPrincipalResolver r = new X509SerialNumberPrincipalResolver(16, false);
        final X509Certificate mockCert = mock(X509Certificate.class);
        when(mockCert.getSerialNumber()).thenReturn(BigInteger.valueOf(300L));

        final String principal = r.resolvePrincipalInternal(mockCert);
        assertEquals("12c", principal);
    }

    @Test
    public void verifyHexPrincipalEven() {
        final X509SerialNumberPrincipalResolver r = new X509SerialNumberPrincipalResolver(16, true);
        final X509Certificate mockCert = mock(X509Certificate.class);
        when(mockCert.getSerialNumber()).thenReturn(BigInteger.valueOf(60300L));

        final String principal = r.resolvePrincipalInternal(mockCert);
        assertEquals("eb8c", principal);
    }
}
