package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * This is {@link X509CommonNameEDIPIPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Parameterized.class)
public class X509CommonNameEDIPIPrincipalResolverTests extends AbstractX509CertificateTests {
    private final X509CommonNameEDIPIPrincipalResolver resolver;
    private final String expected;
    private X509Certificate certificate;

    /**
     * Creates a new test instance with the given parameters.
     *
     * @param certPath       path to the cert
     * @param expectedResult the result expected from the test
     * @param alternatePrincipalAttribute fallback principal attribute (optional)
     */
    public X509CommonNameEDIPIPrincipalResolverTests(
            final String certPath,
            final String expectedResult,
            final String alternatePrincipalAttribute) {

        this.resolver = new X509CommonNameEDIPIPrincipalResolver();
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
    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() {
        val params = new ArrayList<Object[]>();

        // test with cert with EDIPI and no alternate
        params.add(new Object[] {
            "/edipi.cer",
            "1234567890",
            null,
        });

        // test with alternate parameter and cert without EDIPI
        params.add(new Object[] {
            "/user-valid.crt",
            "CN=Alice, OU=CAS, O=Jasig, L=Westminster, ST=Colorado, C=US",
            "subjectDn",
        });

        return params;
    }

    @Test
    public void verifyResolvePrincipalInternal() {
        val userId = this.resolver.resolvePrincipalInternal(this.certificate);
        assertEquals(this.expected, userId);
    }
}
