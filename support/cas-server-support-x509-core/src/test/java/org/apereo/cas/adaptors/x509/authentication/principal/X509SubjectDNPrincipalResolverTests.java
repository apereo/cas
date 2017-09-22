package org.apereo.cas.adaptors.x509.authentication.principal;

import static org.junit.Assert.*;

import java.security.cert.X509Certificate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0
 *
 */
public class X509SubjectDNPrincipalResolverTests extends AbstractX509CertificateTests {

    private final X509SubjectDNPrincipalResolver
        resolver = new X509SubjectDNPrincipalResolver();

    @Test
    public void verifyResolvePrincipalInternal() {
        final X509CertificateCredential c = new X509CertificateCredential(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        assertEquals(VALID_CERTIFICATE.getSubjectDN().getName(), this.resolver.resolve(c,
                CoreAuthenticationTestUtils.getPrincipal(),
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

}
