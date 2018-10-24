package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Test;
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
public class X509SubjectAlternativeNameUPNPrincipalResolverTests {

    private final X509SubjectAlternativeNameUPNPrincipalResolver resolver;
    private final String expected;
    private X509Certificate certificate;

    /**
     * Creates a new test instance with the given parameters.
     *
     * @param certPath       path to the cert
     * @param expectedResult the result expected from the test
     * @param alternatePrincipalAttribute alternate principal attribute (optional)
     */
    public X509SubjectAlternativeNameUPNPrincipalResolverTests(
            final String certPath,
            final String expectedResult,
            final String alternatePrincipalAttribute) {

        this.resolver = new X509SubjectAlternativeNameUPNPrincipalResolver();
        this.resolver.setAlternatePrincipalAttribute(alternatePrincipalAttribute);
        try {
            this.certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
                new FileInputStream(getClass().getResource(certPath).getPath()));
        } catch (final Exception e) {
            fail(String.format("Error parsing certificate %s: %s", certPath, e.getMessage()));
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
        val params = new ArrayList<Object[]>();

        // test with cert with UPN and no alternate
        params.add(new Object[]{
            "/x509-san-upn-resolver.crt",
            "test-user@some-company-domain",
            null,
        });

        // test with alternate parameter and cert with UPN
        params.add(new Object[]{
            "/x509-san-upn-resolver.crt",
            "test-user@some-company-domain",
            "subjectDn",
        });

        // test with alternate parameter and cert without UPN
        params.add(new Object[]{
            "/user-valid.crt",
            "CN=Alice, OU=CAS, O=Jasig, L=Westminster, ST=Colorado, C=US",
            "subjectDn",
        });

        // test with bad alternate parameter and cert without UPN
        params.add(new Object[]{
            "/user-valid.crt",
            null,
            "badAttribute",
        });
        return params;
    }

    @Test
    public void verifyResolvePrincipalInternal() {
        val userId = this.resolver.resolvePrincipalInternal(this.certificate);
        assertEquals(this.expected, userId);

        val credential = new X509CertificateCredential(new X509Certificate[]{this.certificate});
        credential.setCertificate(this.certificate);
        val principal = this.resolver.resolve(credential);
        if (expected != null) {
            assertNotNull(principal);
            assertFalse(principal.getAttributes().isEmpty());
        }
    }

}
