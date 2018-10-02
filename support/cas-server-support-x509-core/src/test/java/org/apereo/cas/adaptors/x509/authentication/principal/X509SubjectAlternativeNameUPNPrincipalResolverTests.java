package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit test for {@link X509SubjectAlternativeNameUPNPrincipalResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
@Slf4j
public class X509SubjectAlternativeNameUPNPrincipalResolverTests {

    private X509Certificate certificate;
    private final X509SubjectAlternativeNameUPNPrincipalResolver resolver;
    private final String expected;

    /**
     * Creates a new test instance with the given parameters.
     *
     * @param certPath path to the cert
     * @param expectedResult the result expected from the test
     */
    public X509SubjectAlternativeNameUPNPrincipalResolverTests(
            final String certPath,
            final String expectedResult) {

        this.resolver = new X509SubjectAlternativeNameUPNPrincipalResolver();
        try {
            this.certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
                    new FileInputStream(getClass().getResource(certPath).getPath()));
        } catch (final Exception e) {
            Assert.fail(String.format("Error parsing certificate %s: %s", certPath, e.getMessage()));
        }
        this.expected = expectedResult;
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() {
        final Collection<Object[]> params = new ArrayList<>();

        params.add(new Object[] {
                "/x509-san-upn-resolver.crt",
                "test-user@some-company-domain"
        });
        return params;
    }

    @Test
    public void verifyResolvePrincipalInternal() {
        final String userId = this.resolver.resolvePrincipalInternal(this.certificate);
        assertEquals(this.expected, userId);
        final X509CertificateCredential credential = new X509CertificateCredential(new X509Certificate[]{this.certificate});
        credential.setCertificate(this.certificate);
        final Principal principal = this.resolver.resolve(credential);
        assertNotNull(principal);
        assertFalse(principal.getAttributes().isEmpty());
    }

}
