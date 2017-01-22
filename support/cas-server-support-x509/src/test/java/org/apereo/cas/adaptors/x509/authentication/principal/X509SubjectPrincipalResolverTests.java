package org.apereo.cas.adaptors.x509.authentication.principal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Unit test for {@link X509SubjectPrincipalResolver}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(Parameterized.class)
public class X509SubjectPrincipalResolverTests {

    private X509Certificate certificate;
    private final X509SubjectPrincipalResolver resolver;
    private final String expected;

    /**
     * Creates a new test instance with the given parameters.
     *
     * @param certPath path to the cert
     * @param descriptor the descriptor
     * @param expectedResult the expected result
     */
    public X509SubjectPrincipalResolverTests(
            final String certPath,
            final String descriptor,
            final String expectedResult) {

        this.resolver = new X509SubjectPrincipalResolver(descriptor);
        try {
            this.certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
                    new FileInputStream(certPath));
        } catch (final Exception e) {
            Assert.fail(String.format("Error parsing certificate %s: %s", certPath, e.getMessage()));
        }
        this.expected = expectedResult;
    }

    /**
     * Gets the unit test parameters.
     *
     * @return  Test parameter data.
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {
        final Collection<Object[]> params = new ArrayList<>();

        // Test case #1
        // Use CN for principal ID
        params.add(new Object[] {
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$CN",
                "Hizzogarthington I.S. Pleakinsense"
        });

        // Test case #2
        // Use email address for principal ID
        params.add(new Object[] {
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$EMAILADDRESS",
                "hizzy@vt.edu"
        });

        // Test case #2
        // Use combination of ou and cn for principal ID
        params.add(new Object[] {
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$OU $CN",
                "Middleware Hizzogarthington I.S. Pleakinsense"
        });

        // Test case #3
        // Use combination of serial number and cn for principal ID
        params.add(new Object[] {
                new ClassPathResource("x509-ctop-resolver-gazzo.crt").getFile().getCanonicalPath(),
                "$CN:$SERIALNUMBER",
                "Gazzaloddi P. Wishwashington:271828183"
        });

        // Test case #4
        // Build principal ID from multivalued attributes
        params.add(new Object[] {
                new ClassPathResource("x509-ctop-resolver-jacky.crt").getFile().getCanonicalPath(),
                "$UID@$DC.$DC",
                "jacky@vt.edu"
        });

        return params;
    }

    @Test
    public void verifyResolvePrincipalInternal() {
        Assert.assertEquals(this.expected, this.resolver.resolvePrincipalInternal(this.certificate));
    }

}
