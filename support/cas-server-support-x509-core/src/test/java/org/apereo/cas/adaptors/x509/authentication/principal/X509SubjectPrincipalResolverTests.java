package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit test for {@link X509SubjectPrincipalResolver}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("X509")
public class X509SubjectPrincipalResolverTests {

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() throws IOException {
        return Stream.of(
            /* Test case #1
             * Use CN for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$CN",
                "Hizzogarthington I.S. Pleakinsense"
            ),

            /* Test case #2
             * Use email address for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$EMAILADDRESS",
                "hizzy@vt.edu"
            ),

            /* Test case #2
             * Use combination of ou and cn for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-hizzy.crt").getFile().getCanonicalPath(),
                "$OU $CN",
                "Middleware Hizzogarthington I.S. Pleakinsense"
            ),

            /* Test case #3
             * Use combination of serial number and cn for principal ID
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-gazzo.crt").getFile().getCanonicalPath(),
                "$CN:$SERIALNUMBER",
                "Gazzaloddi P. Wishwashington:271828183"
            ),

            /* Test case #4
             * Build principal ID from multivalued attributes
             */
            arguments(
                new ClassPathResource("x509-ctop-resolver-jacky.crt").getFile().getCanonicalPath(),
                "$UID@$DC.$DC",
                "jacky@vt.edu"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void verifyResolvePrincipalInternal(final String certPath,
                                               final String descriptor,
                                               final String expectedResult) throws CertificateException, FileNotFoundException {
        val resolver = new X509SubjectPrincipalResolver(descriptor);
        val certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
            new FileInputStream(certPath));

        assertEquals(expectedResult, resolver.resolvePrincipalInternal(certificate));
    }

}
