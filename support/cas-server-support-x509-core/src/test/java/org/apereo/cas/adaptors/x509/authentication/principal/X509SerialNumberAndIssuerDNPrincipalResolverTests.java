package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0.6
 */
public class X509SerialNumberAndIssuerDNPrincipalResolverTests extends AbstractX509CertificateTests {

    private final X509SerialNumberAndIssuerDNPrincipalResolver resolver =
        new X509SerialNumberAndIssuerDNPrincipalResolver(null, null);

    @Test
    public void verifyResolvePrincipalInternal() {
        val c = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);


        val value = "SERIALNUMBER="
            + VALID_CERTIFICATE.getSerialNumber().toString()
            + ", " + VALID_CERTIFICATE.getIssuerDN().getName();

        assertEquals(value, this.resolver.resolve(c, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler())).getId());
    }

    @Test
    public void verifySupport() {
        val c = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(c));
    }

    @Test
    public void verifySupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }

}
